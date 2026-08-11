// =============================================================
// DokuPdf — Root Build Script
// Status: SKELETON
// Lihat PROGRESS.md dan TODO.md di root project untuk status
// keseluruhan pembangunan.
// =============================================================
plugins {
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("com.google.devtools.ksp") version "1.9.24-1.0.20" apply false
}

// TODO 🟢 Tambahkan konfigurasi versi terpusat (libs.versions.toml) saat modul mulai bertambah banyak,
//         supaya versi dependency tidak diduplikasi di app/build.gradle.kts dan modul lain.
