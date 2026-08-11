package com.dokupdf.app.imageproc

import android.graphics.Bitmap

/**
 * Status: SKELETON
 * Mencakup Potong (crop) dan 5 varian filter warna dari referensi UI:
 * Tanpa Tulisan Tangan, H&P, Hemat, Grayscale, Balik.
 *
 * PENTING: semua fungsi di sini menerima & mengembalikan Bitmap yang
 * SUDAH didecode lewat core/imaging/SafeBitmapDecoder — modul ini
 * tidak bertanggung jawab atas decoding awal dari file.
 */
interface ImageProcEngine {
    fun crop(source: Bitmap, region: CropRegion): Bitmap
    fun applyColorFilter(source: Bitmap, filter: ColorFilter): Bitmap
}

data class CropRegion(val left: Float, val top: Float, val right: Float, val bottom: Float)

enum class ColorFilter {
    ORIGINAL,
    NO_HANDWRITING,   // "Tanpa Tulisan Tangan"
    BLACK_WHITE,      // "H&P"
    ECONOMY,          // "Hemat"
    GRAYSCALE,
    INVERT            // "Balik"
}

// Implementasi nyata: lihat StandardImageProcEngine.kt (di package yang sama).
// File ini sengaja hanya berisi kontrak (interface + tipe data) supaya tetap
// ringkas dibaca terpisah dari detail algoritma bitmap.

// TODO 🟢 Pertimbangkan pecah ColorFilter jadi sealed class jika nanti tiap filter
//         butuh parameter berbeda (mis. threshold custom untuk BLACK_WHITE) —
//         enum polos sudah cukup selama semua filter tanpa parameter.
