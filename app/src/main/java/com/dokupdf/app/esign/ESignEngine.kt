package com.dokupdf.app.esign

import android.graphics.Bitmap
import java.io.File

/** Status: SKELETON — Tanda tangani, Tambah/Hapus Tanda Air. Prioritas 🟡. */
interface ESignEngine {
    suspend fun applySignature(pdf: File, signature: Bitmap, pageIndex: Int, x: Float, y: Float): File
    suspend fun addWatermark(pdf: File, watermarkText: String, opacity: Float = 0.3f): File
    suspend fun removeWatermark(pdf: File): File
}

class UnimplementedESignEngine : ESignEngine {
    override suspend fun applySignature(pdf: File, signature: Bitmap, pageIndex: Int, x: Float, y: Float): File =
        error("applySignature() belum diimplementasi")
    override suspend fun addWatermark(pdf: File, watermarkText: String, opacity: Float): File =
        error("addWatermark() belum diimplementasi")
    override suspend fun removeWatermark(pdf: File): File =
        error("removeWatermark() belum diimplementasi")
}

// TODO 🟡 Signature pad UI: Compose Canvas + capture path jadi Bitmap transparan (ARGB_8888,
//         ukuran kecil — signature tidak butuh resolusi tinggi, aman untuk memori).
// TODO 🟡 addWatermark(): overlay teks via PDFBox PDPageContentStream di setiap halaman.
// TODO 🟢 removeWatermark(): "Hapus Penanda" — perlu deteksi elemen watermark dulu,
//         lebih kompleks, tunda sampai addWatermark stabil.
