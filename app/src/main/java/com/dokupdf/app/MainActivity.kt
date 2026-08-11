package com.dokupdf.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import dagger.hilt.android.AndroidEntryPoint

/**
 * Status: SEBAGIAN JALAN (naik dari SKELETON, Tahap 4)
 *
 * Memuat NavHost 4-tab (Beranda|File|Alat|Saya) nyata — lihat DokuPdfNavHost.kt.
 * Alur inti Beranda -> Scan -> Preview -> Simpan sudah tersambung end-to-end
 * (TODO.md bagian 🔴 "UI alur inti"), meski BELUM diuji di device fisik.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // TODO 🟡 Bungkus dengan tema Compose kustom (warna/tipografi DokuPdf) alih-alih
            //         MaterialTheme default begitu identitas visual app ditentukan.
            MaterialTheme {
                Surface {
                    com.dokupdf.app.DokuPdfNavHost()
                }
            }
        }
    }
}
