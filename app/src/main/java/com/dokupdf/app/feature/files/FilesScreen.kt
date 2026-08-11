package com.dokupdf.app.feature.files

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dokupdf.app.data.DocumentEntity
import java.text.DateFormat
import java.util.Date

/**
 * Status: SEBAGIAN JALAN (naik dari placeholder, Tahap 6)
 *
 * Tab "File" (§10 dokumen rancangan) — daftar dokumen tersimpan dari
 * `DocumentDao.observeAll()` lewat [FilesViewModel]. Resolusi TODO 🟡
 * "UI tab File" di TODO.md.
 *
 * Belum ada: folder/grouping, search, dan aksi per-dokumen (hapus/rename/bagikan) —
 * sengaja ditunda, lihat TODO 🟡/🟢 di bawah, supaya list dasar ini bisa diuji dulu.
 */
@Composable
fun FilesScreen(viewModel: FilesViewModel = hiltViewModel()) {
    val documents by viewModel.documents.collectAsState()
    val thumbnails by viewModel.thumbnails.collectAsState()

    Scaffold { padding ->
        if (documents.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada dokumen. Scan dokumen pertamamu dari tab Beranda.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(documents, key = { it.id }) { document ->
                    // Trigger dekripsi thumbnail sekali per dokumen saat baris pertama kali
                    // masuk komposisi (FilesViewModel.requestThumbnail sudah self-dedup).
                    LaunchedEffect(document.id) { viewModel.requestThumbnail(document) }
                    DocumentRow(document = document, thumbnail = thumbnails[document.id])
                }
            }
        }
    }
}

@Composable
private fun DocumentRow(document: DocumentEntity, thumbnail: Bitmap?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (thumbnail != null) {
                    Image(
                        bitmap = thumbnail.asImageBitmap(),
                        contentDescription = document.title,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Tampil juga saat dokumen memang tidak punya thumbnail (thumbnailPath
                    // null) MAUPUN saat dekripsi masih berjalan/gagal — dibedakan tidak
                    // krusial untuk MVP karena keduanya sama-sama "belum ada gambar untuk
                    // ditampilkan" dari sudut pandang pengguna.
                    // TODO 🟢 Ganti ke ikon dokumen yang lebih representatif (mis.
                    //         Icons.Filled.InsertDriveFile) begitu material-icons-extended
                    //         ditambahkan — sengaja pakai Folder dulu karena sudah confirmed
                    //         tersedia di material-icons-core (dipakai juga di DokuPdfNavHost).
                    Icon(
                        Icons.Filled.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${document.pageCount} halaman · ${formatDate(document.updatedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatDate(epochMillis: Long): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(epochMillis))

// TODO 🟡 Aksi per-dokumen (buka/dekripsi ke viewer PDF, hapus, rename, bagikan) belum
//         ada — DocumentRow saat ini murni tampilan. Butuh keputusan viewer PDF dulu
//         (di luar cakupan tab File itu sendiri).
// TODO 🟡 folderId di DocumentEntity belum dipakai di sini — semua dokumen tampil flat.
//         Grouping per folder menyusul setelah UI folder (§7/§10 dokumen rancangan) dibuat.
// TODO 🟢 Tambahkan search bar (butuh query DAO baru — lihat TODO di AppDatabase.kt).
// TODO 🟢 Swipe-to-delete atau long-press context menu, mengikuti referensi UI CamScanner.
