package com.dokupdf.app.imageproc

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import androidx.core.graphics.get
import androidx.core.graphics.set
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Status: SEBAGIAN JALAN (naik dari SKELETON)
 *
 * Implementasi nyata pertama dari 5 varian filter (§3.2 dokumen rancangan) + crop.
 * GRAYSCALE, INVERT, BLACK_WHITE, ECONOMY sudah diimplementasikan dengan pendekatan
 * ColorMatrix/thresholding standar. NO_HANDWRITING masih pendekatan sederhana
 * (lihat TODO di bawah) — kualitasnya belum setara app scanner komersial.
 *
 * PENTING (§9 dokumen rancangan): fungsi-fungsi di sini TIDAK melakukan decoding
 * atau downsampling sendiri — pemanggil (UseCase/ViewModel) WAJIB memastikan
 * [source] sudah melalui SafeBitmapDecoder dengan ukuran yang sesuai konteks
 * (thumbnail kecil untuk preview 5-varian, resolusi lebih besar untuk hasil akhir).
 */
class StandardImageProcEngine @Inject constructor() : ImageProcEngine {

    override fun crop(source: Bitmap, region: CropRegion): Bitmap {
        val left = (region.left * source.width).roundToInt().coerceIn(0, source.width - 1)
        val top = (region.top * source.height).roundToInt().coerceIn(0, source.height - 1)
        val right = (region.right * source.width).roundToInt().coerceIn(left + 1, source.width)
        val bottom = (region.bottom * source.height).roundToInt().coerceIn(top + 1, source.height)
        return Bitmap.createBitmap(source, left, top, right - left, bottom - top)
        // Catatan: CropRegion dianggap dalam koordinat NORMALIZED (0f..1f), bukan piksel
        // absolut — supaya region yang sama valid dipakai baik untuk bitmap preview kecil
        // maupun bitmap resolusi penuh saat proses hasil akhir.
    }

    override fun applyColorFilter(source: Bitmap, filter: ColorFilter): Bitmap = when (filter) {
        ColorFilter.ORIGINAL -> source
        ColorFilter.GRAYSCALE -> applyMatrix(source, grayscaleMatrix())
        ColorFilter.INVERT -> applyMatrix(source, invertMatrix())
        ColorFilter.BLACK_WHITE -> applyThreshold(source, threshold = 128)
        ColorFilter.ECONOMY -> applyThreshold(source, threshold = 160) // ambang lebih tinggi -> lebih banyak putih -> hemat tinta saat cetak
        ColorFilter.NO_HANDWRITING -> applyThreshold(source, threshold = 128) // TODO lihat catatan di bawah
    }

    private fun applyMatrix(source: Bitmap, matrix: ColorMatrix): Bitmap {
        val output = Bitmap.createBitmap(source.width, source.height, source.config ?: Bitmap.Config.RGB_565)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { colorFilter = ColorMatrixColorFilter(matrix) }
        canvas.drawBitmap(source, 0f, 0f, paint)
        return output
    }

    private fun grayscaleMatrix(): ColorMatrix = ColorMatrix().apply { setSaturation(0f) }

    private fun invertMatrix(): ColorMatrix = ColorMatrix(
        floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
        )
    )

    /**
     * Thresholding piksel-per-piksel sederhana: ubah tiap piksel jadi hitam/putih
     * berdasarkan luminance. Ini implementasi PALING DASAR untuk H&P/Hemat —
     * cukup untuk MVP tapi tidak seadaptif algoritma binarization app scanner
     * komersial (mis. Otsu's method / adaptive threshold).
     */
    private fun applyThreshold(source: Bitmap, threshold: Int): Bitmap {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = source[x, y]
                val luminance = (Color.red(pixel) * 0.299 + Color.green(pixel) * 0.587 + Color.blue(pixel) * 0.114)
                output[x, y] = if (luminance >= threshold) Color.WHITE else Color.BLACK
            }
        }
        return output
        // TODO 🟡 Loop piksel-per-piksel ini LAMBAT untuk gambar besar (bisa >1 detik di
        //         device low-end untuk foto ukuran penuh). Untuk hasil akhir (bukan preview
        //         thumbnail), pertimbangkan pindah ke RenderScript/native, atau minimal
        //         jalankan di Dispatchers.Default dari pemanggil agar tidak blok UI thread.
    }
}

// =============================================================
// TODO Prioritas modul imageproc (update dari status sebelumnya)
// =============================================================
// TODO 🔴 [Sudah selesai sebagian] applyThreshold() SAAT INI berjalan synchronous di
//         thread pemanggil — pastikan setiap pemanggil (UseCase) menjalankannya di
//         coroutine Dispatchers.Default, terutama untuk preview 5-varian sekaligus.
// TODO 🟡 NO_HANDWRITING ("Tanpa Tulisan Tangan") saat ini HANYA alias dari BLACK_WHITE.
//         Implementasi asli perlu deteksi & hapus goresan tulisan tangan (warna non-hitam
//         cetak) — butuh riset pendekatan (mis. deteksi warna tinta vs teks cetak).
//         Jangan anggap fitur ini selesai hanya karena tidak error lagi.
// TODO 🟡 Tulis unit test untuk crop() (khususnya kasus region di tepi/coerceIn) dan
//         applyColorFilter() untuk GRAYSCALE/INVERT (matrix hasil bisa dites nilai pikselnya).
// TODO 🟢 Pertimbangkan cache hasil filter per halaman (bukan re-compute tiap kali user
//         switch preview) begitu UI editor multi-halaman mulai dibangun.
