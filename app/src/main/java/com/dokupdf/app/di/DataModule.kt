package com.dokupdf.app.di

import android.content.Context
import androidx.room.Room
import com.dokupdf.app.data.AppDatabase
import com.dokupdf.app.data.DocumentDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Status: SEBAGIAN JALAN (naik dari SKELETON)
 *
 * Menyediakan [AppDatabase] & [DocumentDao] sebagai @Singleton lewat Hilt —
 * menggantikan pola manual `AppDatabase.build()` yang sebelumnya jadi
 * placeholder di companion object AppDatabase (lihat TODO 🔴 di AppDatabase.kt,
 * sekarang sudah dipindah ke sini sesuai catatan tersebut).
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "dokupdf.db")
            // TODO 🟡 Tambahkan .fallbackToDestructiveMigration() HANYA selama pra-rilis
            //         (belum ada user nyata). Hapus sebelum rilis publik pertama — lihat
            //         catatan exportSchema di AppDatabase.kt soal migrasi versi.
            .build()

    @Provides
    @Singleton
    fun provideDocumentDao(database: AppDatabase): DocumentDao = database.documentDao()
}

// TODO 🟡 Tambahkan provider untuk FolderDao/OperationDao terpisah di sini begitu
//         UI tab File (dengan folder) & riwayat operasi mulai dipakai nyata oleh UseCase
//         — saat ini AppDatabase baru mengekspos documentDao().
