package com.dokupdf.app.di

import com.dokupdf.app.ai.AiProvider
import com.dokupdf.app.ai.MlKitAiProvider
import com.dokupdf.app.convert.ConvertEngine
import com.dokupdf.app.convert.UnimplementedConvertEngine
import com.dokupdf.app.core.security.AesGcmFileEncryptor
import com.dokupdf.app.core.security.FileEncryptor
import com.dokupdf.app.esign.ESignEngine
import com.dokupdf.app.esign.UnimplementedESignEngine
import com.dokupdf.app.idcard.IdCardEngine
import com.dokupdf.app.idcard.UnimplementedIdCardEngine
import com.dokupdf.app.imageedit.ImageEditEngine
import com.dokupdf.app.imageedit.UnimplementedImageEditEngine
import com.dokupdf.app.imageproc.ImageProcEngine
import com.dokupdf.app.imageproc.StandardImageProcEngine
import com.dokupdf.app.pdftools.PdfToolsEngine
import com.dokupdf.app.pdftools.UnimplementedPdfToolsEngine
import com.dokupdf.app.scan.DocumentScanEngine
import com.dokupdf.app.scan.MlKitDocumentScanEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Status: SKELETON
 *
 * Semua binding di bawah ini SEMENTARA mengarah ke implementasi
 * "Unimplemented*" (stub yang melempar error saat dipanggil).
 * Saat sebuah modul selesai diimplementasikan nyata (lihat urutan
 * prioritas di TODO.md & PROGRESS.md), GANTI binding-nya di sini —
 * ini satu-satunya tempat yang perlu diubah, tidak perlu mengubah
 * UseCase atau UI yang sudah memakai interface-nya.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class EngineModule {

    @Binds @Singleton
    abstract fun bindFileEncryptor(impl: AesGcmFileEncryptor): FileEncryptor
    // Status: implementasi nyata AES-256-GCM + Android Keystore terpasang (Tahap 4).
    // TODO 🔴 Masih ada TODO 🔴 tersisa di AesGcmFileEncryptor.kt (uji device nyata,
    //         terutama fallback StrongBox) sebelum dianggap benar-benar selesai.

    @Binds @Singleton
    abstract fun bindScanEngine(impl: MlKitDocumentScanEngine): DocumentScanEngine
    // Status: implementasi nyata terpasang, interface sudah direvisi agar cocok
    // dengan bentuk API ML Kit (lihat DocumentScanEngine.kt & MlKitDocumentScanEngine.kt).
    // TODO 🔴 Masih ada 2 TODO 🔴 tersisa di MlKitDocumentScanEngine.kt (error handling
    //         startScan, evaluasi RGB_565 vs ARGB_8888) sebelum dianggap benar-benar selesai.

    @Binds @Singleton
    abstract fun bindImageProcEngine(impl: StandardImageProcEngine): ImageProcEngine
    // Status: implementasi nyata terpasang (GRAYSCALE/INVERT/BLACK_WHITE/ECONOMY/crop).
    // TODO 🟡 NO_HANDWRITING masih alias sementara — lihat TODO di StandardImageProcEngine.kt.

    @Binds @Singleton
    abstract fun bindImageEditEngine(impl: UnimplementedImageEditEngine): ImageEditEngine

    @Binds @Singleton
    abstract fun bindPdfToolsEngine(impl: UnimplementedPdfToolsEngine): PdfToolsEngine

    @Binds @Singleton
    abstract fun bindConvertEngine(impl: UnimplementedConvertEngine): ConvertEngine

    @Binds @Singleton
    abstract fun bindESignEngine(impl: UnimplementedESignEngine): ESignEngine

    @Binds @Singleton
    abstract fun bindIdCardEngine(impl: UnimplementedIdCardEngine): IdCardEngine

    @Binds @Singleton
    abstract fun bindAiProvider(impl: MlKitAiProvider): AiProvider
    // Catatan: AiProvider sudah di-bind ke MlKitAiProvider (bukan Unimplemented*) karena
    // ini implementasi yang DITUJU untuk tahap ini (lihat §6 dokumen rancangan), meski
    // isi fungsinya di dalamnya masih stub/error — beda dengan modul lain yang implementasi
    // final-nya sendiri belum ditentukan.
}

// TODO 🟡 Pindahkan binding QrScanner/TimestampStamper/PrintService (utility) ke module
//         terpisah (di/UtilityModule.kt) begitu implementasi nyatanya mulai dikerjakan —
//         jangan tumpuk semua di satu file supaya tetap mudah dinavigasi saat modul bertambah.
