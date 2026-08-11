package com.dokupdf.app.feature.tools

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Status: BELUM DIMULAI (placeholder)
 * Tab "Alat" (§10 dokumen rancangan) — grid data-driven dari List<ToolItem>
 * yang memetakan ke engine terkait (pdftools, convert, esign, idcard, utility).
 * TODO 🟡 Definisikan ToolItem (icon, label, engine target) & grid Compose —
 *         lihat TODO.md § Penting "UI tab Alat (grid data-driven)".
 */
@Composable
fun ToolsScreen() {
    Scaffold { padding ->
        Text("Grid alat — belum diimplementasikan", modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp))
    }
}
