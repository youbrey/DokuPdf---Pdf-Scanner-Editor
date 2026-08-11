package com.dokupdf.app.idcard

import android.graphics.Bitmap

/** Status: SKELETON — Kartu ID (scan 2 sisi digabung 1 halaman). Prioritas 🟢. */
interface IdCardEngine {
    suspend fun combineTwoSides(front: Bitmap, back: Bitmap): Bitmap
}

class UnimplementedIdCardEngine : IdCardEngine {
    override suspend fun combineTwoSides(front: Bitmap, back: Bitmap): Bitmap =
        error("combineTwoSides() belum diimplementasi")
}

// TODO 🟢 Implementasi: susun front & back dalam satu kanvas (atas-bawah atau kiri-kanan,
//         perlu keputusan tata letak), skala kedua bitmap ke lebar sama dulu via
//         SafeBitmapDecoder sebelum digabung. Modul ini BERGANTUNG pada `scan` selesai duluan
//         (butuh 2x hasil capture berurutan).
