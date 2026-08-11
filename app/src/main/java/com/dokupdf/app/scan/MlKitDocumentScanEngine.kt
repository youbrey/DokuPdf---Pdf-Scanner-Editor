package com.dokupdf.app.scan

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import com.dokupdf.app.core.imaging.BitmapDecodeException
import com.dokupdf.app.core.imaging.ImageSizePresets
import com.dokupdf.app.core.imaging.SafeBitmapDecoder
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.File
import javax.inject.Inject

/**
 * Status: SEBAGIAN JALAN
 *
 * Implementasi [DocumentScanEngine] memakai ML Kit Document Scanner (GmsDocumentScanning),
 * yang sudah menyediakan UI kamera + deteksi tepi + koreksi perspektif bawaan — lihat
 * catatan revisi desain di DocumentScanEngine.kt untuk alasan kenapa interface direvisi
 * dari asumsi awal (CameraX custom).
 *
 * Perbaikan dari versi sebelumnya: [extractScannedPages] sekarang WAJIB lewat
 * [SafeBitmapDecoder] (§9 dokumen rancangan) — tidak lagi decode langsung resolusi
 * penuh dari file hasil ML Kit.
 */
class MlKitDocumentScanEngine @Inject constructor() : DocumentScanEngine {

    private val scannerOptions = GmsDocumentScannerOptions.Builder()
        .setGalleryImportAllowed(false) // MVP: sumber hanya kamera; "Impor Gambar" ditangani UseCase terpisah
        .setPageLimit(1)                // TODO 🟡 naikkan setelah mode multi-halaman dikerjakan (lihat TODO.md)
        .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
        .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
        .build()

    override fun startScan(
        activity: Activity,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        onError: (Throwable) -> Unit
    ) {
        GmsDocumentScanning.getClient(scannerOptions)
            .getStartScanIntent(activity)
            .addOnSuccessListener { intentSender ->
                launcher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener { e ->
                // [Tahap 4] Resolusi TODO 🔴: tidak lagi throw langsung — diteruskan
                // sebagai callback error eksplisit supaya ViewModel bisa tampilkan UI
                // state error (mis. "Google Play Services perlu diperbarui").
                onError(e)
            }
    }

    override fun extractScannedPages(resultIntent: Intent?): List<Bitmap> {
        val result = GmsDocumentScanningResult.fromActivityResultIntent(resultIntent)
            ?: return emptyList()

        return result.pages.orEmpty().mapNotNull { page ->
            val path = page.imageUri.path ?: return@mapNotNull null
            val bitmap = try {
                SafeBitmapDecoder.decodeScaled(
                    path = path,
                    reqWidth = ImageSizePresets.DOCUMENT_PAGE_MAX_DIM,
                    reqHeight = ImageSizePresets.DOCUMENT_PAGE_MAX_DIM,
                    // [Tahap 5] Keputusan RGB_565 vs ARGB_8888 (resolusi TODO 🔴 sebelumnya):
                    // pakai ARGB_8888 di sini karena ini halaman FINAL yang akan disimpan
                    // permanen & dibagikan/dicetak — bukan preview/thumbnail sekali-lihat.
                    // RGB_565 membuang 3 bit per channel warna, yang secara visual bisa
                    // terlihat sebagai banding pada gradasi halus (mis. bayangan di kertas
                    // hasil foto), dan berpotensi menggeser hasil thresholding di
                    // StandardImageProcEngine.applyThreshold() (luminance dihitung dari
                    // channel warna yang sudah terpotong presisinya). Biaya memori: ~16MB
                    // per halaman pada 2000x2000 (vs ~8MB di RGB_565) — dapat diterima
                    // selama setPageLimit(1) belum dinaikkan; PERLU DIEVALUASI ULANG saat
                    // mode multi-halaman (TODO 🟡) mulai memproses beberapa halaman sekaligus.
                    config = Bitmap.Config.ARGB_8888
                )
            } catch (e: BitmapDecodeException) {
                // Halaman ini gagal didecode (file korup/tidak ada) — lewati, jangan
                // gagalkan seluruh hasil scan kalau halaman lain masih valid.
                // TODO 🟡 Saat ini kegagalan senyap (mapNotNull membuang null). Pemanggil
                //         (ScanViewModel) tidak tahu ada halaman yang hilang — pertimbangkan
                //         mengembalikan Result per halaman alih-alih List<Bitmap> polos
                //         begitu mode multi-halaman mulai dikerjakan.
                null
            } finally {
                // [Tahap 5] Cleanup file cache sementara ML Kit — resolusi TODO 🔴
                // "belum ada cleanup, berisiko menumpuk". Dihapus di finally supaya tetap
                // dibersihkan walau decode gagal, karena page.imageUri sudah tidak
                // dibutuhkan lagi setelah SafeBitmapDecoder membaca isinya ke memori.
                File(path).delete()
            }
            bitmap
        }
    }
}

// =============================================================
// TODO Prioritas modul scan
// =============================================================
// [Tahap 5] SELESAI: keputusan RGB_565 vs ARGB_8888 — pakai ARGB_8888 untuk halaman
// final (lihat komentar inline di extractScannedPages()).
// [Tahap 5] SELESAI: cleanup file cache sementara ML Kit (page.imageUri) di blok finally.
// TODO 🟡 setPageLimit(1) perlu dinaikkan begitu mode multi-halaman dikerjakan (TODO.md 🟡).
//         Saat itu terjadi, EVALUASI ULANG biaya memori ARGB_8888 di atas — beberapa
//         halaman 2000x2000 ARGB_8888 sekaligus di memori bisa jadi berat di device 2GB RAM.
// TODO 🟢 Evaluasi SCANNER_MODE_FULL vs SCANNER_MODE_BASE — mode FULL sudah menerapkan
//         filter ML Kit sendiri, berpotensi tumpang tindih dengan filter kustom di
//         imageproc. Belum diuji visual apakah hasilnya konsisten/tidak dobel proses.
// TODO 🔴 Belum diuji sama sekali di device fisik — semua di atas baru tervalidasi lewat
//         pembacaan kode & kesesuaian dengan dokumentasi API, bukan run nyata. Ini sekarang
//         satu-satunya blocker 🔴 tersisa di modul ini (lihat TODO.md untuk status global).
