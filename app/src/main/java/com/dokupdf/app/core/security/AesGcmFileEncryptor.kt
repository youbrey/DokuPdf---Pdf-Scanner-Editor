package com.dokupdf.app.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

/**
 * Status: SEBAGIAN JALAN (naik dari SKELETON)
 *
 * Implementasi nyata [FileEncryptor] memakai AES-256-GCM dengan kunci yang
 * disimpan di Android Keystore (kunci tidak pernah ada dalam bentuk yang bisa
 * diekstrak dari device — sesuai §8 dokumen rancangan).
 *
 * Format file terenkripsi (ditulis oleh [encrypt]):
 *   [12 byte IV][ciphertext + 16 byte GCM tag]
 * IV disimpan bersama file karena IV tidak rahasia, hanya wajib unik per enkripsi.
 *
 * PENTING: kunci Keystore ini per-device (tidak ikut backup/restore, tidak bisa
 * dipindah ke device lain) — sesuai keputusan "full offline, tanpa akun cloud"
 * di §11 dokumen rancangan. Jika nanti ada fitur restore-ke-device-baru, ini
 * HARUS didesain ulang (mis. key wrapping dengan passphrase user).
 */
class AesGcmFileEncryptor @Inject constructor() : FileEncryptor {

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    override suspend fun encrypt(plainFile: File, outputFile: File) = withContext(Dispatchers.IO) {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        }
        val iv = cipher.iv // GCM: 12 byte, digenerate otomatis oleh provider saat init ENCRYPT_MODE

        try {
            FileOutputStream(outputFile).use { out ->
                out.write(iv)
                plainFile.inputStream().use { input ->
                    val buffer = ByteArray(STREAM_BUFFER_SIZE)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        val encryptedChunk = cipher.update(buffer, 0, read)
                        if (encryptedChunk != null) out.write(encryptedChunk)
                    }
                    cipher.doFinal()?.let { out.write(it) }
                }
            }
        } catch (t: Throwable) {
            // [Audit Tahap 6] Sebelumnya: kalau exception terjadi di tengah penulisan
            // (mis. cipher error, disk penuh separuh jalan), outputFile PARSIAL/KORUP
            // dibiarkan tergeletak di disk dan plainFile TIDAK dihapus — pemanggil bisa
            // salah kira save() gagal total padahal ada file .enc korup tertinggal.
            // Sekarang: file parsial dihapus eksplisit sebelum exception diteruskan,
            // plainFile TETAP tidak dihapus (masih satu-satunya salinan valid).
            outputFile.delete()
            throw t
        }

        // Plaintext WAJIB dihapus setelah berhasil dienkripsi (§8 dokumen rancangan) —
        // hanya dihapus jika penulisan output sukses tanpa exception di atas.
        if (!plainFile.delete()) {
            // [Tahap 5] Keputusan: lempar exception eksplisit, jangan diam-diam gagal.
            // Ini kebocoran keamanan (plaintext dokumen pengguna tersisa di disk) —
            // pemanggil (ScanViewModel.save(), dst.) WAJIB tahu dan bisa menindaklanjuti
            // (mis. retry delete, atau tampilkan peringatan), bukan menganggap save() sukses
            // padahal plaintext masih ada. outputFile (hasil enkripsi) TETAP ditulis sukses
            // di titik ini, jadi ini bukan kegagalan simpan dokumen — murni kegagalan cleanup.
            throw PlaintextCleanupFailedException(plainFile)
        }
    }

    override suspend fun decryptToTemp(encryptedFile: File): File = withContext(Dispatchers.IO) {
        val tempFile = File.createTempFile("dokupdf_dec_", ".tmp", encryptedFile.parentFile)
        // TODO 🟡 Pemanggil WAJIB menghapus tempFile ini setelah selesai dipakai — lihat
        //         kontrak di FileEncryptor.kt. Belum ada mekanisme otomatis (mis. tracked
        //         cleanup di WorkManager) untuk menjamin ini terjadi kalau app crash
        //         di tengah proses pemakaian tempFile.

        encryptedFile.inputStream().use { input ->
            val iv = ByteArray(GCM_IV_LENGTH_BYTES)
            val ivRead = input.read(iv)
            require(ivRead == GCM_IV_LENGTH_BYTES) {
                "File terenkripsi rusak atau bukan format DokuPdf: IV tidak lengkap"
            }

            val cipher = Cipher.getInstance(TRANSFORMATION).apply {
                init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
            }

            FileOutputStream(tempFile).use { out ->
                val buffer = ByteArray(STREAM_BUFFER_SIZE)
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    val chunk = cipher.update(buffer, 0, read)
                    if (chunk != null) out.write(chunk)
                }
                // doFinal() di sini juga memvalidasi GCM auth tag — kalau file dimodifikasi
                // atau kunci salah, ini melempar AEADBadTagException.
                cipher.doFinal()?.let { out.write(it) }
            }
        }
        tempFile
    }

    private fun getOrCreateKey(): SecretKey {
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            // TODO 🟢 Pertimbangkan setUserAuthenticationRequired(true) + biometric gate
            //         kalau nanti ada permintaan fitur "kunci app dengan sidik jari" —
            //         belum ada di dokumen rancangan saat ini, jangan tambahkan prematur.
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "dokupdf_file_encryption_key_v1"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH_BYTES = 12
        private const val GCM_TAG_LENGTH_BITS = 128
        private const val STREAM_BUFFER_SIZE = 8 * 1024
    }
}

/**
 * Dilempar oleh [AesGcmFileEncryptor.encrypt] saat file hasil enkripsi berhasil
 * ditulis, TAPI file plaintext sumber gagal dihapus setelahnya. Tipe khusus supaya
 * pemanggil bisa membedakan ini dari kegagalan enkripsi itu sendiri — lihat komentar
 * di titik pelemparan untuk detail kenapa ini tidak dianggap kegagalan simpan biasa.
 */
class PlaintextCleanupFailedException(plainFile: File) :
    Exception("Gagal menghapus file plaintext setelah enkripsi: ${plainFile.absolutePath}")

// =============================================================
// TODO Prioritas modul core.security
// =============================================================
// TODO 🔴 Belum diuji di device nyata (termasuk kasus StrongBox-backed keystore yang
//         tidak tersedia di sebagian device low-end — Keystore API di atas seharusnya
//         fallback otomatis, tapi perlu divalidasi, bukan diasumsikan). Satu-satunya
//         blocker 🔴 tersisa di modul ini setelah [PlaintextCleanupFailedException]
//         ditambahkan di Tahap 5.
// TODO 🟡 Tambahkan unit/instrumented test: encrypt() lalu decryptToTemp() harus
//         menghasilkan byte yang identik dengan file asli (round-trip test).
// TODO 🟡 Tambahkan penanganan eksplisit untuk AEADBadTagException di decryptToTemp()
//         (file korup/tidak cocok kunci) — saat ini akan naik sebagai exception mentah
//         ke pemanggil, belum ada pesan yang ramah untuk ditampilkan ke UseCase/UI.
// [Audit Tahap 6] SELESAI: encrypt() sekarang menghapus outputFile parsial/korup kalau
//         gagal di tengah penulisan, bukan meninggalkannya di disk (lihat blok try/catch).
