package com.dokupdf.app.document

import com.dokupdf.app.imageproc.ColorFilter

/**
 * Status: SKELETON
 * Preset mode scan khusus: Buku, Slide, Papan Tulis — BUKAN engine baru,
 * hanya kombinasi default parameter dari modul `scan` + `imageproc` yang sudah ada.
 * Prioritas 🟢 — kerjakan setelah `scan` inti stabil (lihat TODO.md).
 */
enum class DocumentPreset(val defaultFilter: ColorFilter, val label: String) {
    BOOK(ColorFilter.NO_HANDWRITING, "Buku"),
    SLIDE(ColorFilter.ORIGINAL, "Slide"),
    WHITEBOARD(ColorFilter.NO_HANDWRITING, "Papan Tulis")
}

// TODO 🟢 Tentukan crop ratio default per preset (mis. Buku = rasio 2 halaman berdampingan)
//         setelah modul `imageproc.crop` punya implementasi nyata untuk diacu.
// TODO 🟢 Preset ini murni konfigurasi — pastikan TIDAK membuat logic bitmap baru di sini,
//         semua pemrosesan tetap lewat ImageProcEngine yang sudah ada (hindari duplikasi).
