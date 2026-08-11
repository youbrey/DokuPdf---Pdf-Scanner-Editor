# DokuPdf — TODO (Prioritas Global)

Prioritas: 🔴 Kritis (blocker alur inti) · 🟡 Penting (dibutuhkan MVP) · 🟢 Nanti (pasca-MVP)

## 🔴 Kritis — harus selesai sebelum app bisa dites end-to-end
- [x] Implementasi `imageproc.BitmapDecoder` aman-memori → SafeBitmapDecoder sudah ada
- [x] Implementasi `scan` nyata → MlKitDocumentScanEngine sudah ada dan sudah di-bind ke Hilt
- [x] Selesaikan mismatch desain interface `DocumentScanEngine` → interface DIREVISI agar cocok bentuk API ML Kit (startScan + extractScannedPages)
- [x] Perbaiki `MlKitDocumentScanEngine.extractScannedPages()` supaya pakai SafeBitmapDecoder → selesai, pakai preset `ImageSizePresets.DOCUMENT_PAGE_MAX_DIM`
- [x] **[Sisa dari Tahap 3]** `startScan()` masih throw langsung di failure callback → sudah pakai callback `onError` eksplisit (Tahap 4)
- [x] **[Sisa dari Tahap 3]** Evaluasi RGB_565 vs ARGB_8888 untuk hasil scan akhir → diputuskan pakai ARGB_8888 untuk halaman final (Tahap 5), alasan & trade-off didokumentasikan inline di `MlKitDocumentScanEngine.extractScannedPages()`. Evaluasi visual di device fisik tetap tercatat di TODO gabungan "uji device" di bawah.
- [x] Implementasi `core.security.FileEncryptor` nyata (AES-256-GCM + Android Keystore) → `AesGcmFileEncryptor` (Tahap 4), sudah di-bind di Hilt. Masih perlu uji device nyata (lihat 🔴 baru di bawah).
- [x] Implementasi `data.DocumentDao` + Room database nyata → sudah lewat `di/DataModule.kt` (Tahap 4). Skema masih sama, migrasi belum relevan.
- [x] UI alur inti: Beranda → tombol Scan → Preview hasil → Simpan sebagai dokumen → `DokuPdfNavHost`/`HomeScreen`/`ScanViewModel`/`PreviewScreen` (Tahap 4). **Belum diuji di device fisik.**
- [x] Pastikan `StandardImageProcEngine.applyThreshold()` dijalankan di Dispatchers.Default oleh pemanggil → `ScanViewModel.applyFilter()` sudah memanggilnya di `Dispatchers.Default` (Tahap 4).
- [x] Bersihkan file cache sementara hasil ML Kit (`page.imageUri`) setelah SafeBitmapDecoder selesai decode → sudah dihapus di blok `finally` (Tahap 5), dijalankan baik decode sukses maupun gagal.
- [ ] Uji seluruh implementasi baru (`imageproc`, `scan`, `core.security`, UI alur inti, tab File Tahap 6) di device fisik low-end — **BELUM PERNAH dijalankan sama sekali**, baru diperiksa lewat pembacaan kode/audit statis. Tetap satu-satunya blocker 🔴 murni "belum diuji"; per keputusan eksplisit, pekerjaan 🟡 dilanjutkan meski blocker ini belum lolos (lingkungan pengembangan tidak punya device/emulator/toolchain Android) — lihat riwayat Tahap 6.
- [x] **[BARU — Tahap 6, audit statis]** `SafeBitmapDecoder.decodeScaled()` tidak menangkap `OutOfMemoryError` walau docstring mengklaim begitu — `BitmapFactory.decodeFile` melempar `Error`, bukan mengembalikan null, saat OOM. Sekarang dibungkus eksplisit jadi `BitmapDecodeException`.
- [x] **[BARU — Tahap 6, audit statis]** `AesGcmFileEncryptor.encrypt()` meninggalkan `outputFile` parsial/korup di disk kalau gagal di tengah penulisan (mis. disk penuh). Sekarang dihapus eksplisit di catch sebelum exception diteruskan.
- [ ] **[BARU — Tahap 4]** Struktur folder `feature/*` sebelumnya rusak (direktori literal `{core/...}` akibat bug brace-expansion shell) — sudah diperbaiki, tapi **cek ulang skrip/tool apa pun yang dipakai untuk generate skeleton project ini**, supaya bug yang sama tidak terulang saat modul berikutnya digenerate dengan cara serupa.
- [x] **[BARU — Tahap 4]** `AesGcmFileEncryptor`: jika `plainFile.delete()` gagal setelah enkripsi sukses → sekarang melempar `PlaintextCleanupFailedException` eksplisit (Tahap 5), ditangkap `ScanViewModel.save()` sebagai UI state error.
- [ ] **[BARU — Tahap 4]** `ScanViewModel` di-scope ke NavBackStackEntry route Preview — state `rawBitmap` hilang jika user mundur ke Beranda lalu maju lagi tanpa scan ulang (perilaku disengaja untuk MVP, tapi perlu diputuskan apakah ini UX yang diterima).
- [x] **[BARU — Tahap 5]** `SafeBitmapDecoder.decodeScaled()` melempar generic `error()` untuk semua kegagalan decode → sekarang `BitmapDecodeException` bertipe khusus, ditangkap eksplisit di `MlKitDocumentScanEngine` per-halaman (halaman gagal dilewati, bukan menggagalkan seluruh hasil scan).

## 🟡 Penting — dibutuhkan untuk MVP lengkap sesuai rancangan
- [x] Implementasi 4 dari 5 filter warna (H&P, Hemat, Grayscale, Balik) → sudah ada di StandardImageProcEngine
- [ ] Tanpa Tulisan Tangan (NO_HANDWRITING) — MASIH alias sementara dari H&P, belum implementasi asli (butuh deteksi warna tinta vs teks cetak)
- [x] Implementasi Potong (crop) — logic sudah ada di StandardImageProcEngine.crop(), UI drag-corner di Compose belum dibuat
- [ ] Implementasi `pdftools`: Gabungkan, Pisah, Urutkan Ulang (pakai PDFBox-Android)
- [ ] Implementasi `pdftools.Lock/Unlock` (password PDF, layer terpisah dari enkripsi storage — lihat §8 dokumen rancangan)
- [ ] Implementasi `esign` (signature pad Compose Canvas) + Tambah/Hapus Tanda Air
- [ ] Implementasi `ai.MlKitAiProvider.extractText` (OCR dasar)
- [ ] Implementasi `utility.QrScanner`, `utility.PrintService` (Android Print Framework)
- [x] UI tab File (daftar dokumen, flat tanpa folder) — `FilesViewModel` + `FilesScreen` (Tahap 6), baca `DocumentDao.observeAll()`, thumbnail didekripsi on-demand. Folder/search/aksi per-dokumen masih 🟡/🟢 (lihat TODO baru di FilesScreen.kt)
- [ ] UI tab Alat (grid data-driven dari `List<ToolItem>`)
- [x] Generate thumbnail dokumen (pakai `ImageSizePresets.THUMBNAIL_MAX_DIM`) — `ScanViewModel.save()` (Tahap 6) generate & enkripsi thumbnail; kegagalan generate ditelan jadi `thumbnailPath = null` (belum di-log, lihat TODO di ScanViewModel.kt)
- [ ] **[BARU — Tahap 6]** Aksi buka/lihat dokumen dari tab File (dekripsi ke viewer PDF) — di luar cakupan Tahap 6, butuh keputusan viewer PDF dulu
- [ ] **[BARU — Tahap 6]** `FilesViewModel.thumbnails` cache in-memory tidak pernah dievict — ganti ke LRU sebelum daftar dokumen besar diuji nyata
- [x] **[BARU — Tahap 7]** `app/proguard-rules.pro` direferensikan `build.gradle.kts` (`proguardFiles(...)`) sejak awal tapi filenya tidak pernah ada — ditemukan saat menyiapkan CI. Dibuat dengan keep rule dasar untuk ML Kit/PDFBox/Room, **belum divalidasi lewat build release + uji device nyata** (lihat TODO 🔴 di dalam file itu sendiri).
- [ ] **[BARU — Tahap 7]** CI (`.github/workflows/build-apk.yml`) baru mencakup `assembleDebug` — belum ada job release (butuh keystore signing via GitHub Secrets, keputusan belum diambil) dan belum ada step unit test (belum ada unit test nyata di proyek).
- [ ] **[BARU — Tahap 7]** Proyek belum punya Gradle Wrapper (`gradlew`/`gradle-wrapper.jar`) — CI memakai `gradle/actions/setup-gradle` dengan versi Gradle di-pin manual. Commit wrapper begitu proyek pernah dibuka sekali di Android Studio, lalu update workflow untuk pakai `./gradlew` (lihat TODO di build-apk.yml).
- [x] **[BARU — Tahap 8]** `gradle.properties` tidak pernah ada sejak skeleton Tahap 1 — root cause BUILD FAILED nyata pertama di CI (`checkDebugAarMetadata`: `android.useAndroidX` tidak diaktifkan padahal semua dependency proyek AndroidX). File dibuat dengan `android.useAndroidX=true` + `android.nonTransitiveRClass=true` + heap JVM. **Belum diverifikasi ulang lolos CI** (menunggu run berikutnya) dan `nonTransitiveRClass` belum diuji di device (lihat TODO 🟡 di dalam file itu sendiri).
- [x] **[BARU — Tahap 9]** BUILD FAILED ke-2 di CI: `com.google.mlkit:document-scanner` gagal resolve karena Gradle ikut mencarinya ke Maven Central (429 Too Many Requests) padahal artefak itu cuma ada di `google()`. Ditambahkan `exclusiveContent` di `settings.gradle.kts` supaya grup `com.android.*`/`androidx.*`/`com.google.android.*`/`com.google.mlkit.*` hanya dicari di `google()`. **Belum diverifikasi ulang lolos CI.**

## 🟢 Nanti — pasca-MVP / bisa menyusul
- [ ] `convert` (PDF↔Word/Excel/PPT via Apache POI) — tunda karena menambah ukuran APK signifikan, perlu keputusan (lihat dokumen rancangan §12 poin 4)
- [ ] `idcard` (template kartu ID 2 sisi)
- [ ] `document` preset Buku/Slide/Papan Tulis (setelah `scan` inti stabil)
- [ ] `ai.solve` (Solver AI) dan `ai.translatePhoto` (Penerjemahan foto)
- [ ] `imageedit.SmartErase` (Hapus Cerdas / inpainting) — kompleks, jangan diprioritaskan sebelum alur inti stabil
- [ ] `utility.CountCam`
- [ ] `utility.RestorePhoto` (Pulihkan Foto)
- [ ] Titik ekstensi `CloudAiProvider` (lihat §6 dokumen rancangan) — hanya kalau MVP on-device sudah cukup dan ada kebutuhan akurasi lebih tinggi
- [ ] Tab Saya (pengaturan) — belum krusial selama app full offline tanpa akun

## Aturan Kerja
- Setiap file source code WAJIB punya header dokumentasi (status modul, tanggung jawab file) dan blok `// TODO` dengan prioritas 🔴/🟡/🟢 di bagian bawah file.
- Update `PROGRESS.md` setiap kali status modul naik.
- Jangan mulai modul 🟡/🟢 sebelum semua 🔴 di atas selesai — supaya ada alur inti yang benar-benar bisa dites, bukan banyak modul setengah jadi.
