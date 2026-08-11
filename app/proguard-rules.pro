# =============================================================
# DokuPdf — ProGuard/R8 rules (release build)
# Status: SKELETON — dibuat Tahap 7 karena build.gradle.kts sudah
# mereferensikan file ini (proguardFiles(...)) sejak awal tapi filenya
# belum pernah ada. Tanpa file ini, `assembleRelease` gagal di tahap
# konfigurasi Gradle — ditemukan saat menyiapkan CI build APK.
#
# Aturan default AGP (getDefaultProguardFile) sudah menangani kasus umum
# Android (Activity/Service/dll). Baris di bawah HANYA untuk library pihak
# ketiga yang dipakai proyek ini dan diketahui butuh keep rule tambahan
# saat di-minify — belum divalidasi lewat build release nyata (lihat TODO).
# =============================================================

# ML Kit (document scanner, text recognition, barcode, translate) memakai
# reflection untuk sebagian model on-device — keep rule konservatif.
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_document_scanner.** { *; }

# PDFBox-Android
-keep class com.tom_roush.pdfbox.** { *; }
-keep class com.tom_roush.fontbox.** { *; }
-dontwarn com.tom_roush.pdfbox.**

# Room (@Entity/@Dao sudah di-keep otomatis oleh consumer rules Room,
# baris ini jaga-jaga untuk TypeConverters kalau di-obfuscate)
-keepclassmembers class com.dokupdf.app.data.Converters { *; }

# TODO 🔴 Aturan di atas BELUM divalidasi lewat build release + uji device nyata —
#         minifyEnabled = true bisa saja menghapus/mengubah nama kelas yang
#         sebenarnya masih dibutuhkan reflection ML Kit/PDFBox di runtime.
#         WAJIB uji assembleRelease + install APK hasil release di device fisik
#         sebelum rilis publik pertama, bukan hanya assembleDebug di CI.
# TODO 🟡 Tambahkan keep rule untuk Hilt-generated classes JIKA muncul crash
#         "class not found" terkait Dagger/Hilt di build release — pola umum
#         Hilt biasanya sudah aman lewat consumer ProGuard rules bawaan library,
#         tapi belum terverifikasi khusus untuk kombinasi dependency proyek ini.
