package com.dokupdf.app.feature.scanflow

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dokupdf.app.imageproc.ColorFilter

/**
 * Status: SEBAGIAN JALAN (baru, Tahap 4)
 *
 * Tampilan Preview alur inti: pilih salah satu dari 5 varian filter
 * (§3.2 dokumen rancangan), lalu Simpan. Crop belum ada UI-nya (lihat TODO
 * di ScanViewModel.kt) — MVP ini fokus alur Scan -> Preview -> Simpan dulu.
 */
@Composable
fun PreviewScreen(
    scanViewModel: ScanViewModel = hiltViewModel(),
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val uiState by scanViewModel.uiState.collectAsState()
    var title by remember { mutableStateOf("") }

    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            when (val state = uiState) {
                is ScanUiState.Preview -> {
                    Image(
                        bitmap = state.bitmap.asImageBitmap(),
                        contentDescription = "Hasil scan",
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    )

                    androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(FILTER_OPTIONS) { (filter, label) ->
                            FilterChip(
                                selected = state.activeFilter == filter,
                                onClick = { scanViewModel.applyFilter(filter) },
                                label = { Text(label) }
                            )
                        }
                    }

                    androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Nama dokumen") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { scanViewModel.save(title) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Simpan") }

                    androidx.compose.material3.TextButton(onClick = {
                        scanViewModel.reset()
                        onCancel()
                    }) { Text("Batal") }
                }

                is ScanUiState.Saving -> CircularProgressIndicator()

                is ScanUiState.Saved -> {
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        scanViewModel.reset()
                        onSaved()
                    }
                }

                is ScanUiState.Error -> {
                    Text("Gagal menyimpan: ${state.message}")
                    Button(onClick = onCancel) { Text("Kembali") }
                }

                else -> onCancel()
            }
        }
    }
}

private val FILTER_OPTIONS = listOf(
    ColorFilter.ORIGINAL to "Asli",
    ColorFilter.BLACK_WHITE to "H&P",
    ColorFilter.ECONOMY to "Hemat",
    ColorFilter.GRAYSCALE to "Grayscale",
    ColorFilter.INVERT to "Balik",
    ColorFilter.NO_HANDWRITING to "Tanpa Tulisan Tangan"
    // Catatan: NO_HANDWRITING masih alias BLACK_WHITE di StandardImageProcEngine —
    // tetap ditampilkan di UI supaya alur bisa diuji end-to-end, tapi hasilnya
    // BELUM beda nyata dari H&P sampai TODO 🟡 di StandardImageProcEngine.kt selesai.
)

// TODO 🟡 Preview 5-varian di referensi UI menampilkan thumbnail semua filter sekaligus
//         berdampingan (bukan 1 gambar besar + chip). MVP ini pakai chip dulu supaya
//         alur inti selesai lebih cepat — revisit UI ini setelah alur tervalidasi jalan.
