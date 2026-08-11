// =============================================================
// DokuPdf — app module build script
// Status: SKELETON
//
// Catatan dependensi (lihat TODO.md untuk prioritas implementasi):
// - CameraX + ML Kit    → untuk modul `scan` (prioritas 🔴)
// - PDFBox-Android       → untuk modul `pdftools` (prioritas 🟡)
// - Apache POI (convert) → SENGAJA BELUM dipasang, karena menambah
//   ukuran APK signifikan. Dokumen rancangan §12 poin 4 minta
//   keputusan eksplisit dulu sebelum modul `convert` diaktifkan.
//   Lihat TODO.md bagian 🟢.
// =============================================================
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.dokupdf.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dokupdf.app"
        minSdk = 24          // jangkau device low-end, tetap didukung CameraX & ML Kit
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0-skeleton"
    }

    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    // --- Core / Compose ---
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation(platform("androidx.compose:compose-bom:2024.09.03"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")

    // --- DI ---
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    // TODO 🟢 Tambahkan androidx.compose.material:material-icons-extended HANYA jika
    //         nanti butuh ikon di luar set "Filled" dasar (Home/Person/Build/Folder yang
    //         dipakai DokuPdfNavHost.kt sekarang semuanya ada di material-icons-core,
    //         included transitively lewat material3 — belum perlu extended).

    // --- Kamera & Vision (modul scan, ai, utility) ---
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")
    implementation("com.google.mlkit:text-recognition:16.0.1")
    implementation("com.google.android.gms:play-services-mlkit-document-scanner:16.0.0")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation("com.google.mlkit:translate:17.0.3")

    // --- PDF (modul pdftools) ---
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")

    // --- Storage & keamanan (modul data, core.security) ---
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // --- Background job (untuk proses berat per halaman) ---
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    testImplementation("junit:junit:4.13.2")

    // TODO 🟢 Tambahkan Apache POI di sini HANYA setelah modul `convert` resmi diprioritaskan
    //         (lihat TODO.md § Nanti). Jangan tambahkan lebih awal — perbesar APK tanpa manfaat
    //         selama modul convert belum dikerjakan.
}
