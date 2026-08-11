package com.dokupdf.app.feature.profile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Status: BELUM DIMULAI (placeholder)
 * Tab "Saya" (§10 dokumen rancangan) — pengaturan. Non-kritis untuk MVP
 * offline tanpa akun (lihat TODO.md § Nanti).
 */
@Composable
fun ProfileScreen() {
    Scaffold { padding ->
        Text("Pengaturan — belum diimplementasikan", modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp))
    }
}
