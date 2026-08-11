package com.dokupdf.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Status: SKELETON — lihat §7 dokumen rancangan. */
@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val parentId: String? = null
)

/**
 * Status: SKELETON
 * Riwayat aksi per dokumen (mis. "MERGE", "WATERMARK", "LOCK") — bukan
 * undo/redo per keystroke, hanya jejak operasi tingkat file.
 */
@Entity(tableName = "operation_history")
data class OperationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentId: String,
    val operationType: String,
    val payloadJson: String,
    val timestamp: Long
)

// TODO 🟢 Definisikan konstanta operationType (enum/sealed) alih-alih String bebas,
//         setelah modul pdftools & convert mulai menulis riwayat operasi nyata.
