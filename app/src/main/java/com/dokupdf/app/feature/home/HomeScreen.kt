package com.dokupdf.app.feature.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dokupdf.app.feature.scanflow.ScanUiState
import com.dokupdf.app.feature.scanflow.ScanViewModel

/**
 * Status: SEBAGIAN JALAN (baru, Tahap 4)
 *
 * Tab "Beranda" (§10 dokumen rancangan) — MVP hanya berisi tombol Scan besar.
 * Grid pintasan alat lain (mis. Gabung PDF, Tanda Tangan) menyusul setelah
 * tab Alat (feature/tools) mulai diisi data nyata.
 */
@Composable
fun HomeScreen(
    scanViewModel: ScanViewModel = hiltViewModel(),
    onScanSucceeded: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? androidx.activity.ComponentActivity
    val uiState by scanViewModel.uiState.collectAsState()

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        scanViewModel.onScanResult(result.resultCode, result.data)
    }

    // Pindah ke Preview otomatis begitu hasil scan siap.
    androidx.compose.runtime.LaunchedEffect(uiState) {
        if (uiState is ScanUiState.Preview) onScanSucceeded()
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("DokuPdf")
            androidx.compose.foundation.layout.Spacer(Modifier.height(32.dp))

            when (uiState) {
                is ScanUiState.Scanning -> CircularProgressIndicator()
                is ScanUiState.Error -> Text("Gagal: ${(uiState as ScanUiState.Error).message}")
                else -> {}
            }

            androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
            Button(onClick = {
                if (activity != null) {
                    scanViewModel.startScan(activity, scanLauncher)
                }
                // TODO 🔴 Jika activity == null (Composable dipanggil dari konteks non-Activity),
                //         tombol ini diam-diam tidak melakukan apa pun — seharusnya tidak terjadi
                //         karena HomeScreen selalu dihost dari MainActivity, tapi belum ada
                //         pengaman/log eksplisit untuk kasus ini.
            }) {
                Text("Scan Dokumen")
            }
        }
    }
}

// TODO 🟡 Tambahkan permintaan izin CAMERA eksplisit (runtime permission) sebelum
//         memanggil startScan() — saat ini mengandalkan ML Kit Document Scanner
//         meminta izin sendiri di dalam flow-nya, belum divalidasi apakah itu cukup
//         di semua versi Android yang didukung (minSdk 24).
