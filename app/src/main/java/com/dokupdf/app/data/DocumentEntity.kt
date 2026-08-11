package com.dokupdf.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Status: SKELETON — skema final mengikuti §7 dokumen rancangan.
 *
 * Satu baris = satu hasil (PDF scan, gambar, atau file konversi).
 * filePathEncrypted SELALU menunjuk file yang sudah diproses lewat
 * [com.dokupdf.app.core.security.FileEncryptor] — tidak pernah path plaintext.
 */
@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: DocType,
    val filePathEncrypted: String,
    val thumbnailPath: String?,
    val pageCount: Int,
    val sizeBytes: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val isLocked: Boolean = false,
    val folderId: String? = null
)

enum class DocType { PDF, IMAGE, SCAN, DOCX, XLSX, PPTX }

// Catatan: TypeConverter untuk enum DocType SUDAH ADA (lihat class Converters di
// AppDatabase.kt, dipasang lewat @TypeConverters(Converters::class) di level @Database) —
// TODO sebelumnya di sini sudah tidak relevan, dihapus per Tahap 4.
// TODO 🟡 Tambahkan index pada kolom folderId setelah UI tab File (dengan folder) mulai dibuat,
//         supaya query daftar dokumen per folder tetap cepat saat data banyak.
