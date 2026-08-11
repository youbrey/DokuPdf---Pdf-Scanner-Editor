package com.dokupdf.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dokupdf.app.feature.files.FilesScreen
import com.dokupdf.app.feature.home.HomeScreen
import com.dokupdf.app.feature.profile.ProfileScreen
import com.dokupdf.app.feature.scanflow.PreviewScreen
import com.dokupdf.app.feature.tools.ToolsScreen

/**
 * Status: SEBAGIAN JALAN (baru, Tahap 4)
 *
 * Implementasi NavHost 4-tab sesuai §10 dokumen rancangan, menggantikan
 * placeholder Text() sebelumnya di MainActivity.kt. Preview BUKAN salah satu
 * dari 4 tab — dia route terpisah yang dituju dari tab Beranda (mengikuti
 * alur inti "Beranda -> Scan -> Preview -> Simpan" di TODO.md).
 */
private sealed class BottomTab(val route: String, val label: String) {
    data object Home : BottomTab("home", "Beranda")
    data object Files : BottomTab("files", "File")
    data object Tools : BottomTab("tools", "Alat")
    data object Profile : BottomTab("profile", "Saya")
}

private const val ROUTE_PREVIEW = "preview"

private val bottomTabs = listOf(BottomTab.Home, BottomTab.Files, BottomTab.Tools, BottomTab.Profile)

@Composable
fun DokuPdfNavHost() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = backStackEntry?.destination
            // Sembunyikan bottom bar di route Preview — ini bukan tab, tapi langkah
            // sekali-jalan dalam alur Scan -> Preview -> Simpan.
            if (currentDestination?.route != ROUTE_PREVIEW) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon(), contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BottomTab.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(BottomTab.Home.route) {
                HomeScreen(onScanSucceeded = { navController.navigate(ROUTE_PREVIEW) })
            }
            composable(BottomTab.Files.route) { FilesScreen() }
            composable(BottomTab.Tools.route) { ToolsScreen() }
            composable(BottomTab.Profile.route) { ProfileScreen() }
            composable(ROUTE_PREVIEW) {
                PreviewScreen(
                    onSaved = {
                        navController.navigate(BottomTab.Files.route) {
                            popUpTo(BottomTab.Home.route)
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
        }
    }
}

private fun BottomTab.icon() = when (this) {
    BottomTab.Home -> Icons.Filled.Home
    BottomTab.Files -> Icons.Filled.Folder
    BottomTab.Tools -> Icons.Filled.Build
    BottomTab.Profile -> Icons.Filled.Person
}

// TODO 🟡 ScanViewModel di-scope ke masing-masing route (hiltViewModel() default scope ke
//         NavBackStackEntry) — artinya rawBitmap di ViewModel HILANG kalau user navigasi
//         balik dari Preview ke Home lalu maju lagi tanpa scan ulang. Ini SENGAJA untuk MVP
//         (state scan sekali-pakai), tapi catat di sini supaya tidak dianggap bug nanti.
