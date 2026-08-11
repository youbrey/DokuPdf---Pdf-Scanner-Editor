package com.dokupdf.app.feature.files

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokupdf.app.core.imaging.BitmapDecodeException
import com.dokupdf.app.core.imaging.ImageSizePresets
import com.dokupdf.app.core.imaging.SafeBitmapDecoder
import com.dokupdf.app.core.security.FileEncryptor
import com.dokupdf.app.data.DocumentDao
import com.dokupdf.app.data.DocumentEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Status: SEBAGIAN JALAN (baru, Tahap 6)
 *
 * Menyediakan data untuk tab "File" (§10 dokumen rancangan). `documents` langsung
 * mengikuti `DocumentDao.observeAll()` (sudah siap dipakai sejak Tahap 4/5, lihat
 * TODO.md § Penting "UI tab File" — resolusi TODO itu).
 *
 * Thumbnail TIDAK ditampilkan langsung dari `thumbnailPath` (path itu menunjuk file
 * TERENKRIPSI, §8 dokumen rancangan — tidak bisa dibaca langsung sebagai gambar).
 * ViewModel ini mendekripsi tiap thumbnail ke file sementara on-demand lewat
 * `FileEncryptor.decryptToTemp()`, decode via `SafeBitmapDecoder`, lalu simpan hasilnya
 * di cache in-memory `thumbnails`. Cache di sini SENGAJA hidup selama ViewModel hidup
 * (bukan LRU) — untuk daftar dokumen skala MVP (puluhan, bukan ribuan) ini cukup;
 * TODO 🟡 di bawah mencatat kapan ini perlu diganti LRU.
 */
@HiltViewModel
class FilesViewModel @Inject constructor(
    documentDao: DocumentDao,
    private val fileEncryptor: FileEncryptor,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    val documents: StateFlow<List<DocumentEntity>> = documentDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _thumbnails = MutableStateFlow<Map<String, Bitmap?>>(emptyMap())
    val thumbnails: StateFlow<Map<String, Bitmap?>> = _thumbnails.asStateFlow()

    // Mencegah dekripsi ganda kalau requestThumbnail() dipanggil berkali-kali (mis. saat
    // LazyColumn recompose) untuk dokumen yang sama sebelum dekripsi pertama selesai.
    private val inFlight = mutableSetOf<String>()
    private val inFlightLock = Mutex()

    /** Dipanggil dari UI (mis. LaunchedEffect per item list) saat sebuah baris jadi terlihat. */
    fun requestThumbnail(document: DocumentEntity) {
        val path = document.thumbnailPath ?: return
        if (_thumbnails.value.containsKey(document.id)) return // sudah ada (termasuk null = gagal)

        viewModelScope.launch {
            val shouldStart = inFlightLock.withLock {
                if (document.id in inFlight) false else {
                    inFlight += document.id
                    true
                }
            }
            if (!shouldStart) return@launch

            val bitmap = withContext(Dispatchers.IO) {
                var tempFile: java.io.File? = null
                try {
                    val encrypted = java.io.File(path)
                    if (!encrypted.exists()) return@withContext null
                    tempFile = fileEncryptor.decryptToTemp(encrypted)
                    SafeBitmapDecoder.decodeScaled(
                        path = tempFile.absolutePath,
                        reqWidth = ImageSizePresets.THUMBNAIL_MAX_DIM,
                        reqHeight = ImageSizePresets.THUMBNAIL_MAX_DIM,
                        config = Bitmap.Config.RGB_565 // thumbnail: hemat memori, bukan hasil final
                    )
                } catch (e: BitmapDecodeException) {
                    null
                } catch (t: Throwable) {
                    // Termasuk AEADBadTagException (file thumbnail korup/kunci berubah) —
                    // TODO 🟡 belum dibedakan dari kegagalan lain, lihat catatan di bawah.
                    null
                } finally {
                    // Kontrak FileEncryptor.decryptToTemp(): pemanggil WAJIB membersihkan
                    // tempFile setelah selesai dipakai (lihat AesGcmFileEncryptor.kt).
                    tempFile?.delete()
                }
            }

            _thumbnails.value = _thumbnails.value + (document.id to bitmap)
            inFlightLock.withLock { inFlight -= document.id }
        }
    }
}

// TODO 🟡 Cache `thumbnails` di atas tidak pernah dievict — untuk daftar dokumen yang
//         besar (ratusan+) ini bisa menumpuk memori. Ganti ke LRU (mis. androidx.collection.LruCache)
//         kalau tab File mulai menampilkan banyak dokumen sekaligus di produksi.
// TODO 🟡 Kegagalan dekripsi thumbnail (AEADBadTagException dkk.) saat ini disamakan
//         dengan "tidak ada thumbnail" (null) — tidak dibedakan secara UI. Cukup untuk
//         MVP karena UI fallback ke ikon generik di kedua kasus, tapi log/telemetry
//         akan berguna untuk membedakan bug nyata dari dokumen lama tanpa thumbnail.
// TODO 🟢 Tambahkan search by title (butuh query DAO baru, lihat TODO di AppDatabase.kt).
