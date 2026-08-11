// Status: SEBAGIAN JALAN (Tahap 9)
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        // [Tahap 9] Batasi grup com.android/androidx/com.google.* supaya HANYA dicari
        // di google() — root cause build gagal ke-2: Gradle sempat ikut mencari
        // `com.google.mlkit:document-scanner` (yang sebetulnya cuma ada di google())
        // ke Maven Central juga, dan kena rate-limit (429 Too Many Requests) dari sana.
        // exclusiveContent() menghentikan pencarian "nyasar" ini di sumbernya, bukan
        // hanya menghindari 429 sesekali — juga mempercepat resolusi dependency karena
        // Gradle tidak lagi query dua repo untuk grup yang jawabannya sudah pasti di google().
        exclusiveContent {
            forRepository { google() }
            filter {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google\\.android.*")
                includeGroupByRegex("com\\.google\\.mlkit.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "DokuPdf"
include(":app")

// TODO 🟡 Kalau nanti muncul "Could not resolve" untuk dependency AndroidX/Google baru
//         yang grupnya BELUM tercakup regex di atas (mis. grup Firebase `com.google.firebase`),
//         tambahkan ke daftar includeGroupByRegex, jangan hapus exclusiveContent block-nya —
//         itu cara yang salah untuk "memperbaiki" masalah serupa (balik ke perilaku lama
//         yang rawan 429).
