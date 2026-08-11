package com.dokupdf.app.convert

import java.io.File

/**
 * Status: SKELETON — SENGAJA belum ada dependency Apache POI terpasang di Gradle.
 * Lihat dokumen rancangan §12 poin 4: keputusan "convert jadi fitur inti MVP atau
 * menyusul" masih perlu dikonfirmasi karena menambah ukuran APK signifikan.
 * Kontrak ini dibuat lebih dulu supaya UseCase lain bisa mulai diintegrasikan
 * tanpa menunggu keputusan itu.
 */
interface ConvertEngine {
    suspend fun pdfToWord(pdf: File): File
    suspend fun pdfToExcel(pdf: File): File
    suspend fun pdfToPpt(pdf: File): File
    suspend fun pdfToImages(pdf: File): List<File>
    suspend fun pdfToLongImage(pdf: File): File
}

class UnimplementedConvertEngine : ConvertEngine {
    override suspend fun pdfToWord(pdf: File): File = error("Belum diimplementasi — lihat TODO.md 🟢, tunggu keputusan Apache POI")
    override suspend fun pdfToExcel(pdf: File): File = error("Belum diimplementasi")
    override suspend fun pdfToPpt(pdf: File): File = error("Belum diimplementasi")
    override suspend fun pdfToImages(pdf: File): List<File> = error("Belum diimplementasi")
    override suspend fun pdfToLongImage(pdf: File): File = error("Belum diimplementasi")
}

// TODO 🟢 pdfToImages() & pdfToLongImage() bisa dikerjakan LEBIH DULU dari pdfToWord/Excel/Ppt
//         karena hanya butuh PDFBox (sudah terpasang), tidak butuh Apache POI. Prioritaskan ini
//         kalau modul convert mulai dikerjakan sebelum keputusan POI final.
// TODO 🟢 pdfToWord/Excel/Ppt: BUTUH keputusan eksplisit dulu (dokumen rancangan §12.4)
//         sebelum menambah dependency Apache POI ke app/build.gradle.kts.
