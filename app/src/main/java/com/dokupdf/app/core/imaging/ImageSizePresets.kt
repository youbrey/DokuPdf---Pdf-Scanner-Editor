package com.dokupdf.app.core.imaging

/**
 * Status: SEBAGIAN JALAN
 * Konstanta ukuran target decode bitmap per konteks penggunaan, supaya semua
 * modul memakai angka yang sama (bukan hardcode angka berbeda-beda tiap file).
 */
object ImageSizePresets {
    /** Untuk thumbnail kecil (mis. daftar file, preview 5-varian filter berdampingan). */
    const val THUMBNAIL_MAX_DIM = 400

    /** Untuk halaman hasil scan yang akan disimpan/diproses lebih lanjut (bukan thumbnail). */
    const val DOCUMENT_PAGE_MAX_DIM = 2000
}

// TODO 🟡 Angka DOCUMENT_PAGE_MAX_DIM (2000px) masih perkiraan awal — sesuaikan setelah
//         ada pengujian nyata: cukup tajam untuk hasil cetak/OCR tapi tidak memicu OOM
//         di device RAM 2GB saat beberapa halaman diproses bersamaan (mis. saat merge PDF).
