package com.dokupdf.app.feature.scanflow

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokupdf.app.core.imaging.ImageSizePresets
import com.dokupdf.app.core.security.FileEncryptor
import com.dokupdf.app.data.DocType
import com.dokupdf.app.data.DocumentDao
import com.dokupdf.app.data.DocumentEntity
import com.dokupdf.app.imageproc.ColorFilter
import com.dokupdf.app.imageproc.ImageProcEngine
import com.dokupdf.app.scan.DocumentScanEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

/**
 * Status: SEBAGIAN JALAN (baru, Tahap 4)
 *
 * Mengorkestrasi alur inti §10 dokumen rancangan: Beranda -> Scan -> Preview -> Simpan.
 * Sengaja HANYA menangani 1 halaman (mengikuti setPageLimit(1) di
 * MlKitDocumentScanEngine) — mode multi-halaman menyusul (lihat TODO 🟡 di sana).
 *
 * Alur data: DocumentScanEngine (bitmap mentah) -> ImageProcEngine (filter, di
 * Dispatchers.Default — resolusi TODO 🔴 applyThreshold blocking di
 * StandardImageProcEngine.kt) -> FileEncryptor (simpan terenkripsi, §8) ->
 * DocumentDao (metadata, §7).
 */
@HiltViewModel
class ScanViewModel @Inject constructor(
    private val scanEngine: DocumentScanEngine,
    private val imageProcEngine: ImageProcEngine,
    private val fileEncryptor: FileEncryptor,
    private val documentDao: DocumentDao,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    /** Bitmap mentah hasil scan (sebelum filter) — disimpan supaya ganti filter tidak perlu scan ulang. */
    private var rawBitmap: Bitmap? = null

    fun startScan(activity: Activity, launcher: ActivityResultLauncher<IntentSenderRequest>) {
        _uiState.value = ScanUiState.Scanning
        scanEngine.startScan(
            activity = activity,
            launcher = launcher,
            onError = { throwable ->
                _uiState.value = ScanUiState.Error(
                    throwable.message ?: "Gagal memulai pemindaian. Coba lagi."
                )
            }
        )
    }

    fun onScanResult(resultCode: Int, resultIntent: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            // User membatalkan flow ML Kit — bukan error, kembali ke Idle diam-diam.
            _uiState.value = ScanUiState.Idle
            return
        }
        viewModelScope.launch {
            val pages = withContext(Dispatchers.Default) {
                scanEngine.extractScannedPages(resultIntent)
            }
            val first = pages.firstOrNull()
            if (first == null) {
                _uiState.value = ScanUiState.Error("Tidak ada halaman terdeteksi dari hasil scan.")
                return@launch
            }
            rawBitmap = first
            applyFilter(ColorFilter.ORIGINAL)
        }
    }

    fun applyFilter(filter: ColorFilter) {
        val source = rawBitmap ?: return
        viewModelScope.launch {
            // Thresholding piksel-per-piksel di StandardImageProcEngine bisa lambat —
            // WAJIB di Dispatchers.Default (lihat TODO 🔴 di StandardImageProcEngine.kt,
            // ini bagian yang menuntaskannya untuk jalur preview).
            val filtered = withContext(Dispatchers.Default) {
                imageProcEngine.applyColorFilter(source, filter)
            }
            _uiState.value = ScanUiState.Preview(bitmap = filtered, activeFilter = filter)
        }
    }

    fun save(title: String) {
        val current = _uiState.value as? ScanUiState.Preview ?: return
        _uiState.value = ScanUiState.Saving

        viewModelScope.launch {
            try {
                val documentId = UUID.randomUUID().toString()
                withContext(Dispatchers.IO) {
                    val documentsDir = File(appContext.filesDir, "documents").apply { mkdirs() }
                    val plainTemp = File.createTempFile("scan_", ".jpg", appContext.cacheDir)
                    FileOutputStream(plainTemp).use { out ->
                        current.bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                    }

                    val encryptedOutput = File(documentsDir, "$documentId.enc")
                    // encrypt() menghapus plainTemp setelah sukses (kontrak FileEncryptor, §8).
                    fileEncryptor.encrypt(plainTemp, encryptedOutput)

                    // [Tahap 6] Generate thumbnail — didownscale ke THUMBNAIL_MAX_DIM lalu
                    // dienkripsi juga (§8: plaintext tidak boleh tersisa permanen di disk,
                    // berlaku sama untuk thumbnail seperti dokumen utama). Kegagalan generate
                    // thumbnail TIDAK menggagalkan seluruh save() — dokumen tetap valid tanpa
                    // thumbnail (UI tab File fallback ke ikon generik, lihat FilesScreen.kt).
                    val thumbnailPath = try {
                        val scale = minOf(
                            1f,
                            ImageSizePresets.THUMBNAIL_MAX_DIM.toFloat() /
                                maxOf(current.bitmap.width, current.bitmap.height)
                        )
                        val thumbBitmap = Bitmap.createScaledBitmap(
                            current.bitmap,
                            (current.bitmap.width * scale).toInt().coerceAtLeast(1),
                            (current.bitmap.height * scale).toInt().coerceAtLeast(1),
                            /* filter = */ true
                        )
                        val plainThumbTemp = File.createTempFile("thumb_", ".jpg", appContext.cacheDir)
                        FileOutputStream(plainThumbTemp).use { out ->
                            thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                        }
                        val encryptedThumb = File(documentsDir, "$documentId.thumb.enc")
                        fileEncryptor.encrypt(plainThumbTemp, encryptedThumb)
                        encryptedThumb.absolutePath
                    } catch (t: Throwable) {
                        null
                    }

                    val now = System.currentTimeMillis()
                    documentDao.upsert(
                        DocumentEntity(
                            id = documentId,
                            title = title.ifBlank { "Dokumen $now" },
                            type = DocType.SCAN,
                            filePathEncrypted = encryptedOutput.absolutePath,
                            thumbnailPath = thumbnailPath,
                            pageCount = 1,
                            sizeBytes = encryptedOutput.length(),
                            createdAt = now,
                            updatedAt = now
                        )
                    )
                }
                _uiState.value = ScanUiState.Saved(documentId = "")
            } catch (t: Throwable) {
                // PlaintextCleanupFailedException (lihat AesGcmFileEncryptor.kt) juga masuk
                // sini — dokumen SUDAH terenkripsi & tersimpan di titik itu, tapi kita tetap
                // tampilkan sebagai error karena pengguna berhak tahu ada plaintext tersisa.
                // TODO 🟡 Bedakan pesan untuk PlaintextCleanupFailedException vs kegagalan
                //         lain begitu ada UseCase terpisah — saat ini pesan generik dari
                //         t.message sudah cukup jelas (lihat teks exception-nya) untuk MVP.
                _uiState.value = ScanUiState.Error(t.message ?: "Gagal menyimpan dokumen.")
            }
        }
    }

    fun reset() {
        rawBitmap = null
        _uiState.value = ScanUiState.Idle
    }
}

sealed interface ScanUiState {
    data object Idle : ScanUiState
    data object Scanning : ScanUiState
    data class Preview(val bitmap: Bitmap, val activeFilter: ColorFilter) : ScanUiState
    data object Saving : ScanUiState
    data class Saved(val documentId: String) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

// TODO 🟡 Tambahkan dukungan multi-halaman: rawBitmap jadi List<Bitmap>, UI Preview
//         dapat pager antar halaman — menyusul setelah setPageLimit dinaikkan
//         (lihat TODO di MlKitDocumentScanEngine.kt).
// TODO 🟡 Tambahkan UseCase terpisah (mis. SaveScannedDocumentUseCase) alih-alih logic
//         penyimpanan langsung di ViewModel begitu alur ini mulai dipakai modul lain
//         (mis. import dari galeri) — untuk sekarang cukup untuk 1 alur scan MVP.
// TODO 🟢 Crop belum diekspos di UI Preview (ImageProcEngine.crop() sudah ada) —
//         tambahkan drag-corner overlay begitu alur dasar ini stabil & teruji.
// [Tahap 6] SELESAI: thumbnail digenerate & dienkripsi di save() — thumbnailPath
//         tidak lagi selalu null. TODO 🟡 baru: kegagalan generate thumbnail saat ini
//         senyap (catch-all ke null) — pertimbangkan log/telemetry supaya kegagalan
//         diam-diam tidak tak-terlihat di produksi.
