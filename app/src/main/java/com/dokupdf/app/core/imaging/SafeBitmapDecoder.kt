package com.dokupdf.app.core.imaging

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

/**
 * Status: SEBAGIAN JALAN (naik dari SKELETON, Tahap 5)
 *
 * WAJIB dipakai oleh SEMUA modul yang men-decode bitmap (imageproc,
 * imageedit, idcard, scan preview, dll). Lihat §9 dokumen rancangan:
 * ini adalah pertahanan utama terhadap OOM di device low-end.
 *
 * Aturan: jangan pernah panggil BitmapFactory.decodeFile/decodeStream
 * langsung di modul lain — selalu lewat fungsi di sini.
 */
object SafeBitmapDecoder {

    /**
     * Decode file bitmap dengan downsampling otomatis ke ukuran target.
     *
     * [config] menentukan trade-off memori vs kualitas warna — lihat
     * [Bitmap.Config.RGB_565] (separuh memori, TANPA alpha, cukup untuk
     * preview/thumbnail) vs [Bitmap.Config.ARGB_8888] (kualitas warna penuh,
     * dipakai untuk halaman dokumen final yang akan disimpan permanen —
     * lihat keputusan di bawah). Default RGB_565 untuk mempertahankan
     * perilaku pemanggil lama yang belum eksplisit memilih.
     *
     * @throws BitmapDecodeException jika file tidak bisa didecode (path tidak
     *   ada, format tidak didukung, atau file korup) — pemanggil (UseCase/
     *   ViewModel) WAJIB menangkap ini dan tampilkan sebagai UI state error,
     *   bukan biarkan generic RuntimeException naik ke crash handler.
     */
    fun decodeScaled(
        path: String,
        reqWidth: Int,
        reqHeight: Int,
        config: Bitmap.Config = Bitmap.Config.RGB_565
    ): Bitmap {
        if (!File(path).exists()) {
            throw BitmapDecodeException("File tidak ditemukan: $path")
        }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            // decodeFile tidak melempar exception untuk file korup/format tidak dikenal —
            // dia diam-diam mengisi outWidth/outHeight dengan -1. Ini satu-satunya sinyal
            // yang tersedia dari BitmapFactory untuk kasus ini.
            throw BitmapDecodeException("Format tidak didukung atau file korup: $path")
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight, reqWidth, reqHeight)
            inPreferredConfig = config
        }
        return try {
            BitmapFactory.decodeFile(path, options)
                ?: throw BitmapDecodeException("Gagal decode bitmap (I/O error): $path")
        } catch (e: OutOfMemoryError) {
            // [Audit Tahap 6] BitmapFactory.decodeFile MELEMPAR OutOfMemoryError (Error,
            // bukan Exception) saat gagal alokasi memori — sebelumnya TIDAK tertangkap
            // di sini sama sekali, walau docstring lama mengklaim "OOM ditangani".
            // Dibungkus jadi BitmapDecodeException supaya pemanggil bisa catch satu
            // tipe konsisten, dan supaya inSampleSize yang sudah dihitung tidak
            // menyembunyikan kegagalan asli sebagai crash mentah.
            throw BitmapDecodeException("Gagal decode bitmap (kehabisan memori): $path")
        }
    }

    private fun calculateSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
        var sampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / sampleSize >= reqHeight && halfWidth / sampleSize >= reqWidth) {
                sampleSize *= 2
            }
        }
        return sampleSize
    }
}

/**
 * Dilempar oleh [SafeBitmapDecoder] saat decode gagal karena file tidak ada,
 * format tidak didukung, file korup, atau OOM saat decode. Tipe khusus
 * (bukan generic RuntimeException) supaya pemanggil bisa catch spesifik ini
 * tanpa menutupi bug lain — resolusi TODO 🔴 sebelumnya di file ini.
 */
class BitmapDecodeException(message: String) : Exception(message)

// TODO 🔴 Tambahkan varian decodeScaled yang menerima ByteArray/InputStream langsung
//         (bukan hanya path file) — dibutuhkan saat `scan` mengambil frame dari CameraX
//         tanpa menulis ke disk dulu.
// [Audit Tahap 6] SELESAI: OutOfMemoryError sekarang eksplisit ditangkap & dibungkus jadi
//         BitmapDecodeException — sebelumnya klaim di docstring meleset dari perilaku
//         nyata BitmapFactory (lihat komentar inline di decodeScaled()).
