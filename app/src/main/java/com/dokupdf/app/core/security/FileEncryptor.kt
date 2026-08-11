package com.dokupdf.app.core.security

import java.io.File

/**
 * Status: SKELETON — kontrak sudah final mengikuti §8 dokumen rancangan,
 * implementasi AES-GCM nyata BELUM ditulis.
 *
 * Tanggung jawab: mengenkripsi file hasil scan/edit sebelum disimpan
 * permanen, dan mendekripsi saat file perlu dibaca kembali.
 *
 * PENTING (lihat §8 & §9 dokumen rancangan):
 * - Plaintext tidak boleh pernah tersimpan permanen di disk.
 * - Ini adalah layer BERBEDA dari fitur "Kunci" (password PDF) di menu
 *   Alat — jangan digabung. FileEncryptor melindungi file selama ada
 *   di storage device; password PDF melindungi file saat dibagikan.
 */
interface FileEncryptor {
    /** Enkripsi [plainFile], tulis hasil ke [outputFile], lalu hapus [plainFile]. */
    suspend fun encrypt(plainFile: File, outputFile: File)

    /** Dekripsi [encryptedFile] menjadi file sementara yang dipakai lalu dibersihkan oleh pemanggil. */
    suspend fun decryptToTemp(encryptedFile: File): File
}

/**
 * Status: DIGANTIKAN oleh [AesGcmFileEncryptor] (lihat AesGcmFileEncryptor.kt) —
 * sudah TIDAK di-bind di EngineModule per Tahap 4. Dipertahankan di source hanya
 * untuk keperluan test/fallback lokal, JANGAN di-bind ulang di Hilt tanpa alasan
 * eksplisit (mis. flavor debug khusus untuk mempercepat iterasi tanpa Keystore).
 */
class NoopFileEncryptor : FileEncryptor {
    override suspend fun encrypt(plainFile: File, outputFile: File) {
        plainFile.copyTo(outputFile, overwrite = true)
        // TODO 🔴 Ganti seluruh isi fungsi ini dengan AES-256-GCM nyata
        //         menggunakan kunci dari Android Keystore (lihat androidx.security.crypto.EncryptedFile).
        //         Setelah enkripsi sukses, plainFile WAJIB dihapus (plainFile.delete())
        //         — saat ini sengaja tidak dihapus karena masih stub/no-op.
    }

    override suspend fun decryptToTemp(encryptedFile: File): File {
        // TODO 🔴 Implementasi dekripsi nyata pasangan dari encrypt() di atas.
        return encryptedFile
    }
}

// TODO 🔴 [PRIORITAS TERTINGGI keamanan] Modul ini harus selesai SEBELUM modul `data`
//         mulai menyimpan dokumen asli pengguna — lihat urutan di PROGRESS.md.
