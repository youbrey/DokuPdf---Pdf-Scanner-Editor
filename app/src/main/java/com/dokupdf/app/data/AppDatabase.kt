package com.dokupdf.app.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.Flow

/**
 * Status: SKELETON — DAO sudah mencakup operasi dasar (list, insert, delete).
 * BELUM ada migrasi versi (belum relevan karena masih versi 1 / belum dirilis).
 */
@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE folderId = :folderId ORDER BY updatedAt DESC")
    fun observeByFolder(folderId: String?): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(document: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun delete(id: String)

    // TODO 🟡 Tambahkan query search by title (dibutuhkan untuk kotak "Pencarian" seperti
    //         yang terlihat di referensi UI CamScanner).
}

class Converters {
    @TypeConverter
    fun fromDocType(value: DocType): String = value.name

    @TypeConverter
    fun toDocType(value: String): DocType = DocType.valueOf(value)
}

@Database(
    entities = [DocumentEntity::class, FolderEntity::class, OperationEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
    // Status: instance singleton sekarang disediakan oleh di/DataModule.kt (Hilt @Provides).
    // Companion object build() manual sebelumnya DIHAPUS sesuai TODO — jangan buat instance
    // AppDatabase manual di luar Hilt, supaya tetap satu instance per proses aplikasi.
}

// TODO 🟡 exportSchema saat ini false untuk mempercepat iterasi skeleton.
//         Set true + simpan schema JSON begitu skema mulai stabil, supaya migrasi
//         versi berikutnya bisa diuji dengan benar (hindari kehilangan data user nanti).
