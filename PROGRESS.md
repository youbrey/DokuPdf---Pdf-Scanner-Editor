# DokuPdf — PROGRESS

Lacak perkembangan pembangunan aplikasi di sini. Update setiap kali ada modul yang naik status.
Status: `RANCANGAN` → `SKELETON` → `SEBAGIAN JALAN` → `SELESAI (MVP)`

| Modul | Status | Catatan |
|---|---|---|
| Struktur project & Gradle | SKELETON | build.gradle root + app dibuat; dependency hilt-navigation-compose & material-icons-core ditambahkan Tahap 4 |
| core.security (enkripsi file) | SEBAGIAN JALAN | AesGcmFileEncryptor (AES-256-GCM + Android Keystore) nyata, di-bind di Hilt. `PlaintextCleanupFailedException` ditambahkan (Tahap 5) — kegagalan hapus plaintext tidak lagi diam-diam. Belum diuji di device nyata. |
| data (Room: documents/folders/history) | SEBAGIAN JALAN | AppDatabase & DocumentDao kini disediakan lewat di/DataModule.kt (Hilt @Provides @Singleton), bukan companion object manual. Skema belum berubah. |
| di (Hilt modules) | SEBAGIAN JALAN | EngineModule di-update (FileEncryptor -> AesGcmFileEncryptor). DataModule baru ditambahkan untuk Room. |
| scan | SEBAGIAN JALAN | Interface DocumentScanEngine DIREVISI agar cocok bentuk API ML Kit nyata. Semua TODO 🔴 tercatat sejak Tahap 3 SELESAI (Tahap 4-5): onError eksplisit, ARGB_8888 untuk halaman final (bukan RGB_565), cleanup cache ML Kit di finally, error per-halaman via BitmapDecodeException. Sisa: belum diuji di device nyata. |
| imageproc | SEBAGIAN JALAN | StandardImageProcEngine nyata: crop, GRAYSCALE, INVERT, BLACK_WHITE, ECONOMY sudah jalan (thresholding sederhana). NO_HANDWRITING masih alias BLACK_WHITE (belum implementasi asli). Sudah di-bind di Hilt & dipanggil dari Dispatchers.Default lewat ScanViewModel. BELUM diuji di device nyata / belum ada unit test. |
| core.imaging (SafeBitmapDecoder) | SEBAGIAN JALAN | Tahap 5: melempar `BitmapDecodeException` bertipe khusus (bukan generic `error()`), menerima parameter `config: Bitmap.Config` untuk pilih RGB_565 (preview/thumbnail) vs ARGB_8888 (hasil final). |
| imageedit | SKELETON | interface Edit Teks/Hapus Cerdas/Ambil Ulang |
| pdftools | SKELETON | interface gabung/pisah/urutkan/kunci |
| convert | SKELETON | interface PDF↔Office, belum ada dependensi POI dipasang |
| esign | SKELETON | interface tanda tangan & watermark |
| idcard | SKELETON | interface kartu ID 2 sisi |
| ai (AiProvider + MlKitAiProvider) | SKELETON | kontrak siap, implementasi ML Kit belum diisi |
| utility | SKELETON | interface Rumus/Stempel Waktu/CountCam/Cetak/QR |
| document (preset Buku/Slide/Papan Tulis) | SKELETON | interface preset |
| UI (4 tab: Beranda/File/Alat/Saya) | SEBAGIAN JALAN | DokuPdfNavHost + NavigationBar 4 tab nyata. Beranda & Preview (alur inti scan) fungsional. Tab File kini nyata (Tahap 6): daftar dokumen + thumbnail terdekripsi. Alat/Saya masih placeholder Text. |

## Riwayat
- **[Tahap 1]** Struktur project, seluruh kontrak/interface Feature Engine dibuat sebagai skeleton, belum ada logic asli. Tujuan tahap ini: memastikan arsitektur (§4–§6 dokumen rancangan) valid dan bisa di-compile sebagai kerangka sebelum diisi implementasi.
- **[Tahap 2]** Mulai implementasi nyata dua modul 🔴 prioritas tertinggi:
  - `imageproc`: filter warna & crop nyata (StandardImageProcEngine), sudah di-bind di Hilt.
  - `scan`: implementasi nyata pakai ML Kit Document Scanner (MlKitDocumentScanEngine).
    **Ditemukan mismatch desain**: interface `DocumentScanEngine` awal diasumsikan bisa
    dipanggil sebagai pure suspend function, padahal API ML Kit sesungguhnya butuh
    Activity + ActivityResultLauncher (pola Android Activity Result API). Interface
    lama BELUM dihapus/diubah, hanya ditandai belum dipakai — lihat TODO 🔴 di
    MlKitDocumentScanEngine.kt untuk keputusan yang masih perlu diambil.
  - Masih ada 2 pelanggaran aturan §9 (SafeBitmapDecoder) yang perlu diperbaiki:
    decode hasil ML Kit langsung tanpa downsampling, dan thresholding piksel-per-piksel
    yang berpotensi lambat di thread pemanggil.
- **[Tahap 3]** Selesaikan 2 masalah 🔴 yang ditemukan di Tahap 2:
  - Interface `DocumentScanEngine` DIREVISI (bukan sekadar ditambal) supaya benar-benar
    merefleksikan bentuk API ML Kit (startScan + extractScannedPages, bukan pure suspend fn).
  - `extractScannedPages()` sekarang memakai `SafeBitmapDecoder` dengan preset ukuran baru
    (`ImageSizePresets.DOCUMENT_PAGE_MAX_DIM`) — tidak lagi decode resolusi penuh langsung.
  - `MlKitDocumentScanEngine` sudah di-bind ke `DocumentScanEngine` di Hilt.
  - Sisa TODO 🔴 di modul scan: error handling `startScan()` masih throw langsung (bukan
    callback error eksplisit), dan keputusan RGB_565 vs ARGB_8888 untuk hasil akhir belum
    diuji visual. Keduanya dicatat di TODO.md.

- **[Tahap 4]** Selesaikan alur inti end-to-end (Beranda -> Scan -> Preview -> Simpan):
  - **Ditemukan & diperbaiki bug struktur project**: skeleton ZIP sebelumnya berisi direktori
    literal bernama `{core/security,core/imaging,...,feature/profile}` (hasil brace-expansion
    shell yang gagal saat `mkdir` dijalankan tanpa bash) — folder `feature/*` yang sesungguhnya
    TIDAK PERNAH tercipta. Direktori rusak ini dihapus dan `feature/{home,files,tools,profile,scanflow}`
    dibuat ulang dengan benar.
  - `core.security.AesGcmFileEncryptor`: implementasi nyata AES-256-GCM + Android Keystore,
    menggantikan `NoopFileEncryptor` di binding Hilt (`EngineModule`).
  - `di.DataModule` baru: `AppDatabase`/`DocumentDao` sekarang `@Provides @Singleton` lewat Hilt,
    menggantikan companion object `AppDatabase.build()` manual (sudah dihapus).
  - `scan.DocumentScanEngine.startScan()`: parameter `onError` ditambahkan — resolusi TODO 🔴
    "throw langsung di failure callback" dari Tahap 3.
  - UI alur inti nyata: `DokuPdfNavHost` (4 tab + route Preview terpisah), `HomeScreen` (tombol
    Scan + ActivityResultLauncher ML Kit), `ScanViewModel` (orkestrasi scan -> filter ->
    enkripsi -> simpan Room, filter dijalankan di `Dispatchers.Default`), `PreviewScreen`
    (chip 5 filter + simpan). Tab File/Alat/Saya masih placeholder.
  - Dependency baru ditambahkan: `androidx.hilt:hilt-navigation-compose`,
    `androidx.compose.material:material-icons-core`.

- **[Tahap 5]** Tuntaskan sisa 🔴 yang eksplisit tercatat sejak Tahap 3/4:
  - `core.imaging.SafeBitmapDecoder`: kini melempar `BitmapDecodeException` bertipe khusus
    (file tidak ada, format tidak didukung, atau file korup) alih-alih generic `error()`.
    Menerima parameter `config: Bitmap.Config` (default RGB_565, kompatibel mundur).
  - `scan.MlKitDocumentScanEngine.extractScannedPages()`: **keputusan RGB_565 vs ARGB_8888
    diambil** — memakai ARGB_8888 untuk halaman final (bukan lagi RGB_565), dengan alasan
    kualitas warna & akurasi thresholding didokumentasikan inline. Halaman yang gagal decode
    kini ditangkap per-halaman (dilewati, bukan menggagalkan seluruh hasil scan). File cache
    sementara ML Kit (`page.imageUri`) sekarang **dihapus di blok `finally`** setelah dibaca —
    resolusi TODO 🔴 "belum ada cleanup, berisiko menumpuk".
  - `core.security.AesGcmFileEncryptor`: kegagalan hapus plaintext setelah enkripsi sukses
    sekarang melempar `PlaintextCleanupFailedException` eksplisit (bukan diam-diam gagal),
    ditangkap `ScanViewModel.save()` sebagai UI state error.
  - **Sisa 🔴 setelah tahap ini hanya satu**: pengujian nyata di device fisik low-end —
    semua yang lain di atas sudah selesai secara kode/desain, tinggal divalidasi jalan.

- **[Tahap 6]** Audit statis (pengganti sementara uji device fisik, yang tidak bisa dijalankan
  di lingkungan pengembangan ini — tanpa Android SDK/emulator/adb/gradle/akses jaringan) +
  lanjut ke modul 🟡 atas keputusan eksplisit:
  - **2 bug ditemukan lewat pembacaan kode teliti** (bukan sekadar re-read TODO lama):
    `SafeBitmapDecoder.decodeScaled()` tidak menangkap `OutOfMemoryError` (docstring
    sebelumnya menyesatkan — mengklaim OOM "ditangani" padahal `BitmapFactory.decodeFile`
    melempar `Error`, bukan return null); `AesGcmFileEncryptor.encrypt()` meninggalkan
    file output parsial/korup di disk kalau gagal di tengah penulisan. Keduanya diperbaiki.
  - **Thumbnail dokumen**: `ScanViewModel.save()` sekarang generate thumbnail (downscale ke
    `THUMBNAIL_MAX_DIM`) dan mengenkripsinya juga (§8: plaintext tidak boleh tersisa, berlaku
    sama untuk thumbnail) — `DocumentEntity.thumbnailPath` tidak lagi selalu null.
  - **Tab File nyata**: `FilesViewModel` (baru) meng-collect `DocumentDao.observeAll()` dan
    mendekripsi thumbnail on-demand per baris (cache in-memory sederhana, belum LRU — lihat
    TODO 🟡). `FilesScreen` (sebelumnya placeholder Text, dan sempat ada bug kompilasi:
    `.dp` dipakai tanpa import) sekarang jadi `LazyColumn` nyata dengan fallback ikon untuk
    dokumen tanpa thumbnail.
  - Belum termasuk di tahap ini: aksi buka/hapus/rename dokumen, folder, search — dicatat
    sebagai TODO 🟡/🟢 baru di TODO.md dan di file masing-masing.

## Urutan Pembangunan Selanjutnya (lihat TODO.md untuk detail)
1. **Uji end-to-end di device fisik low-end** (satu-satunya 🔴 tersisa, murni "belum
   dijalankan") — alur Scan → filter ARGB_8888 → enkripsi AES-GCM → simpan Room + thumbnail →
   tampil di tab File, termasuk kasus gagal (Play Services usang, file korup, storage penuh,
   OOM saat decode thumbnail) yang sekarang punya exception typed untuk ditangani.
2. UI tab Alat (grid data-driven dari `List<ToolItem>`) — modul 🟡 berikutnya yang belum disentuh.
3. Aksi per-dokumen di tab File (buka/lihat, hapus) — butuh keputusan viewer PDF dulu.
4. Modul lain (pdftools, convert, ai, esign, dst.) menyusul.
