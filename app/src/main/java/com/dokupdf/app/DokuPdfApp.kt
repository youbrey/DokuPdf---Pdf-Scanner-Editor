package com.dokupdf.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Status: SKELETON
 *
 * Entry point aplikasi. Titik pemasangan Hilt DI.
 * Belum ada inisialisasi khusus (mis. WorkManager custom config)
 * karena belum ada job background nyata yang berjalan.
 */
@HiltAndroidApp
class DokuPdfApp : Application()

// TODO 🟡 Tambahkan inisialisasi WorkManager custom Configuration di sini
//         saat modul convert/pdftools mulai menjalankan job background nyata,
//         supaya job berat (lihat §9 dokumen rancangan) bisa diatur prioritas & retry-nya.
