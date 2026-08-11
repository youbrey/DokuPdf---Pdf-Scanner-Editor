package com.dokupdf.app.utility

import android.graphics.Bitmap

/**
 * Status: SKELETON
 * Mencakup: Rumus, Stempel Waktu, CountCam, Cetak, Scan Kode QR.
 * Fitur-fitur ini independen satu sama lain — boleh dikerjakan tidak berurutan,
 * pilih berdasarkan kebutuhan berikutnya, kecuali PrintService yang butuh
 * dokumen PDF valid terlebih dulu (bergantung modul pdftools/scan).
 */
interface QrScanner {
    suspend fun scan(bitmap: Bitmap): String?
}

interface TimestampStamper {
    fun applyTimestamp(bitmap: Bitmap, format: String = "dd/MM/yyyy HH:mm"): Bitmap
}

interface PrintService {
    fun printDocument(documentPath: String)
}

class UnimplementedQrScanner : QrScanner {
    override suspend fun scan(bitmap: Bitmap): String? =
        error("scan() belum diimplementasi — rencana pakai com.google.mlkit:barcode-scanning")
}

class UnimplementedTimestampStamper : TimestampStamper {
    override fun applyTimestamp(bitmap: Bitmap, format: String): Bitmap =
        error("applyTimestamp() belum diimplementasi")
}

class UnimplementedPrintService : PrintService {
    override fun printDocument(documentPath: String) {
        error("printDocument() belum diimplementasi — rencana pakai android.print.PrintManager")
    }
}

// TODO 🟡 QrScanner: implementasi paling sederhana di antara utility lain, cocok
//         dikerjakan lebih dulu sebagai "quick win" setelah alur inti scan selesai.
// TODO 🟡 TimestampStamper: overlay teks tanggal di Canvas — sederhana, tidak butuh
//         dependency tambahan.
// TODO 🟢 PrintService: pakai android.print.PrintManager + PrintDocumentAdapter,
//         kerjakan setelah ada minimal 1 dokumen PDF valid yang bisa dites cetak.
// TODO 🟢 Rumus (pengenalan rumus matematika) & CountCam BELUM ada interface —
//         definisikan kontraknya nanti setelah cakupan fitur ini lebih jelas
//         (mis. Rumus pakai OCR khusus simbol matematika, beda dari extractText biasa).
