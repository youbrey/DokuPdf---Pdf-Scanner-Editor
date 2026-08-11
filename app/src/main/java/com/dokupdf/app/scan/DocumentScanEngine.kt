package com.dokupdf.app.scan

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest

/**
 * Status: SEBAGIAN JALAN (direvisi dari SKELETON)
 *
 * REVISI dari desain awal: interface versi sebelumnya (`captureAndDetectEdges()`
 * sebagai suspend fn + `applyPerspectiveCorrection()` terpisah) DIHAPUS karena
 * tidak cocok dengan bentuk API ML Kit Document Scanner yang sesungguhnya:
 * - ML Kit Document Scanner adalah flow UI penuh (bukan API sekali panggil) yang
 *   HARUS dijalankan lewat Activity Result API (IntentSender + ActivityResultLauncher).
 * - Deteksi tepi & koreksi perspektif SUDAH ditangani di dalam flow itu sendiri
 *   (SCANNER_MODE_FULL) — tidak perlu langkah applyPerspectiveCorrection terpisah.
 *
 * Interface ini sekarang merefleksikan bentuk API yang sesungguhnya dipakai.
 * Implementasi: lihat MlKitDocumentScanEngine.kt.
 */
interface DocumentScanEngine {
    /**
     * Mulai flow scan. Harus dipanggil dari Activity, dengan [launcher] yang
     * sudah didaftarkan memakai contract StartIntentSenderForResult.
     * Hasil diterima lewat callback [launcher], lalu diteruskan ke [extractScannedPages].
     *
     * [onError] dipanggil (bukan exception dilempar) jika ML Kit gagal menyiapkan
     * flow scan (mis. Google Play Services tidak tersedia/perlu update) — pemanggil
     * (ViewModel) WAJIB menangani ini sebagai UI state error, bukan membiarkan crash.
     */
    fun startScan(
        activity: Activity,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        onError: (Throwable) -> Unit
    )

    /**
     * Ekstrak halaman hasil scan dari intent yang diterima ActivityResultLauncher
     * pemanggil (dipanggil saat callback launcher menerima Activity.RESULT_OK).
     * Bitmap yang dikembalikan SUDAH melalui SafeBitmapDecoder (§9 dokumen rancangan) —
     * bukan resolusi asli kamera yang bisa memicu OOM.
     */
    fun extractScannedPages(resultIntent: Intent?): List<Bitmap>
}

// [Tahap 4] onError ditambahkan sebagai parameter eksplisit — resolusi TODO 🔴
// "startScan() masih throw langsung" dari Tahap 3. Lihat MlKitDocumentScanEngine.kt.
// TODO 🟡 Tambahkan mode multi-halaman (scan berturut-turut jadi 1 dokumen PDF banyak halaman)
//         — saat ini scannerOptions.setPageLimit(1) di implementasi hanya menangani 1 halaman.
// TODO 🟢 Kalau nanti butuh mock untuk unit test ViewModel (tanpa Activity nyata), buat
//         FakeDocumentScanEngine terpisah di source set test — jangan taruh di source utama.
