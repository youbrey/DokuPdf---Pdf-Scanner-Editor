package com.dokupdf.app.pdftools

import java.io.File

/**
 * Status: SKELETON
 * Mencakup: Gabungkan, Pisah, Urutkan Ulang, Kunci/Buka Kunci PDF.
 * Prioritas 🟡 (lihat TODO.md) — dikerjakan setelah alur inti scan berjalan.
 *
 * CATATAN KEAMANAN (§8 dokumen rancangan): lock()/unlock() di sini adalah
 * password PROTEKSI PDF standar (untuk berbagi file), BUKAN pengganti
 * core.security.FileEncryptor yang melindungi file di storage device.
 */
interface PdfToolsEngine {
    suspend fun merge(files: List<File>, outputName: String): File
    suspend fun split(file: File, pageRanges: List<IntRange>): List<File>
    suspend fun reorderPages(file: File, newPageOrder: List<Int>): File
    suspend fun lock(file: File, password: String): File
    suspend fun unlock(file: File, password: String): File
}

class UnimplementedPdfToolsEngine : PdfToolsEngine {
    override suspend fun merge(files: List<File>, outputName: String): File =
        error("merge() belum diimplementasi — rencana pakai PDFBox-Android")
    override suspend fun split(file: File, pageRanges: List<IntRange>): List<File> =
        error("split() belum diimplementasi")
    override suspend fun reorderPages(file: File, newPageOrder: List<Int>): File =
        error("reorderPages() belum diimplementasi")
    override suspend fun lock(file: File, password: String): File =
        error("lock() belum diimplementasi")
    override suspend fun unlock(file: File, password: String): File =
        error("unlock() belum diimplementasi")
}

// TODO 🟡 Implementasi merge() & split() dengan PDFBox-Android PDDocument — proses PER HALAMAN
//         (lihat §9 dokumen rancangan), jangan load seluruh dokumen sumber ke memori sekaligus
//         kalau file berukuran besar/banyak halaman.
// TODO 🟡 Implementasi lock()/unlock() pakai StandardProtectionPolicy dari PDFBox.
// TODO 🟢 reorderPages(): implementasi setelah merge/split/lock stabil, karena secara teknis
//         bisa dibangun di atas kombinasi split+merge yang sudah ada.
