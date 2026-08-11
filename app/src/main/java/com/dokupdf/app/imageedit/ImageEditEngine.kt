package com.dokupdf.app.imageedit

import android.graphics.Bitmap
import android.graphics.RectF

/**
 * Status: SKELETON
 * Mencakup: Edit Teks, Hapus Cerdas (smart erase), Ambil Ulang.
 * Prioritas 🟢 — jangan dikerjakan sebelum modul scan & imageproc inti selesai (lihat TODO.md).
 */
interface ImageEditEngine {
    /** Hapus objek pada [region] dan isi ulang area tsb (inpainting sederhana). */
    suspend fun smartErase(source: Bitmap, region: RectF): Bitmap

    /** Ambil ulang halaman tertentu — delegasi ke scan engine, disediakan di sini untuk konsistensi alur edit. */
    suspend fun retakePage(pageIndex: Int): Bitmap
}

class UnimplementedImageEditEngine : ImageEditEngine {
    override suspend fun smartErase(source: Bitmap, region: RectF): Bitmap {
        error("smartErase() belum diimplementasi — lihat TODO.md 🟢")
    }
    override suspend fun retakePage(pageIndex: Int): Bitmap {
        error("retakePage() belum diimplementasi")
    }
}

// TODO 🟢 smartErase: mulai dari pendekatan sederhana (fill warna rata-rata sekitar region)
//         sebelum coba inpainting berbasis ML — inpainting kualitas tinggi butuh model
//         tambahan yang menambah ukuran APK, pertimbangkan trade-off ini dulu.
// TODO 🟢 Edit Teks: butuh hasil OCR dari modul `ai` (bounding box tiap kata) sebagai
//         input sebelum bisa reposisi/edit teks di atas gambar — modul ini BERGANTUNG
//         pada `ai.extractText` selesai duluan.
