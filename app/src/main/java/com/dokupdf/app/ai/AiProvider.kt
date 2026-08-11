package com.dokupdf.app.ai

import android.graphics.Bitmap

/**
 * Status: SKELETON — kontrak final sesuai §6 dokumen rancangan (titik ekstensi AI).
 *
 * Implementasi tahap ini: MlKitAiProvider (on-device, gratis, offline).
 * Implementasi masa depan: CloudAiProvider (Claude/GPT/Google Vision) — akan
 * di-inject sebagai binding alternatif via Hilt TANPA mengubah interface ini
 * atau UseCase yang memakainya. JANGAN ubah signature interface ini demi
 * kebutuhan satu provider spesifik.
 */
interface AiProvider {
    suspend fun extractText(bitmap: Bitmap): OcrResult
    suspend fun solve(problemText: String): SolverResult
    suspend fun translatePhoto(bitmap: Bitmap, targetLang: String): TranslationResult
}

data class OcrResult(val fullText: String, val blocks: List<TextBlock>)
data class TextBlock(val text: String, val left: Float, val top: Float, val right: Float, val bottom: Float)
data class SolverResult(val answer: String, val steps: List<String>)
data class TranslationResult(val translatedBitmap: Bitmap)

/**
 * Implementasi on-device menggunakan ML Kit. Prioritas 🟡 untuk extractText
 * (fitur "Ekstrak Teks" berdiri sendiri di menu), 🟢 untuk solve()/translatePhoto()
 * yang lebih kompleks.
 */
class MlKitAiProvider : AiProvider {
    override suspend fun extractText(bitmap: Bitmap): OcrResult {
        error("extractText() belum diimplementasi — rencana pakai com.google.mlkit:text-recognition")
    }
    override suspend fun solve(problemText: String): SolverResult {
        error("solve() belum diimplementasi — MVP awal bisa mulai dari rule-based sederhana untuk soal matematika dasar, sebelum ada CloudAiProvider")
    }
    override suspend fun translatePhoto(bitmap: Bitmap, targetLang: String): TranslationResult {
        error("translatePhoto() belum diimplementasi — rencana pakai com.google.mlkit:translate + extractText di atas")
    }
}

// TODO 🟡 extractText(): implementasi pertama di modul ai, jadi fondasi untuk Edit Teks
//         (imageedit) dan Solver AI (solve()) — kerjakan ini duluan dari 3 fungsi di sini.
// TODO 🟢 solve(): mulai dari cakupan terbatas (aritmatika/aljabar dasar berbasis rule),
//         JANGAN coba cakupan luas dulu — akurasi solver umum butuh model besar/cloud.
// TODO 🟢 translatePhoto(): bergantung pada extractText() + ML Kit Translate API,
//         kerjakan paling akhir dari tiga fungsi AI ini.
// TODO 🟢 Buat CloudAiProviderStub (belum aktif) sebagai placeholder implementasi kedua,
//         supaya struktur DI (di/AiModule.kt) sudah terbiasa punya >1 implementasi sejak awal.
