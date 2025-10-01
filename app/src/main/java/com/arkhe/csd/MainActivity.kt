@file:Suppress("SpellCheckingInspection")

package com.arkhe.csd

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.arkhe.csd.ui.theme.CopyShareDownloadTheme
import com.arkhe.csd.utils.ClipboardManager
import com.arkhe.csd.utils.ImageGenerator
import com.arkhe.csd.utils.ImageSection
import com.arkhe.csd.utils.PdfGenerator
import com.arkhe.csd.utils.PdfSection
import com.arkhe.csd.utils.ShareManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val clipboardManager: ClipboardManager by inject()
    private val shareManager: ShareManager by inject()
    private val pdfGenerator: PdfGenerator by inject()
    private val imageGenerator: ImageGenerator by inject()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Permission diperlukan untuk menyimpan file", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkStoragePermission()

        setContent {
            CopyShareDownloadTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        clipboardManager = clipboardManager,
                        shareManager = shareManager,
                        pdfGenerator = pdfGenerator,
                        imageGenerator = imageGenerator
                    )
                }
            }
        }
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    clipboardManager: ClipboardManager,
    shareManager: ShareManager,
    pdfGenerator: PdfGenerator,
    imageGenerator: ImageGenerator
) {
    val scope = rememberCoroutineScope()

    // Separate loading states for each button
    var isLoadingSharePdfSimple by remember { mutableStateOf(false) }
    var isLoadingSharePdfCustom by remember { mutableStateOf(false) }
    var isLoadingDownloadPdfSimple by remember { mutableStateOf(false) }
    var isLoadingDownloadPdfCustom by remember { mutableStateOf(false) }
    var isLoadingShareImage by remember { mutableStateOf(false) }
    var isLoadingDownloadImage by remember { mutableStateOf(false) }

    // Sample data
    val sampleText = """
        🎉 Halo dari Aplikasi Copy Share Download! 🎉
        
        📱 Ini adalah contoh text dengan emoji yang bisa di-copy
        ✅ Format tetap rapi ketika di-paste ke WhatsApp
        🚀 Sangat mudah digunakan!
        
        Terima kasih telah menggunakan aplikasi ini! 💙
    """.trimIndent()

    val sampleTextWithPlaceholders = """
        🎫 Invoice: {{invoice_number}}
        
        👤 Nama: {{customer_name}}
        📧 Email: {{customer_email}}
        💰 Total: Rp {{total_amount}}
        📅 Tanggal: {{date}}
        
        Terima kasih atas pembelian Anda! 🙏
    """.trimIndent()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Copy, Share & Download") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Simple Copy
            SectionCard(
                title = "1. Copy Text Sederhana",
                description = "Copy text dan emoji yang tampil"
            ) {
                Text(
                    text = sampleText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Button(
                    onClick = { clipboardManager.copyToClipboard(sampleText) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📋 Copy Text")
                }
            }

            // Section 2: Copy with Injection
            SectionCard(
                title = "2. Copy dengan Data Injection",
                description = "Copy dengan mengisi placeholder"
            ) {
                Text(
                    text = sampleTextWithPlaceholders,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Button(
                    onClick = {
                        val injectedData = mapOf(
                            "invoice_number" to "INV-2024-001",
                            "customer_name" to "Budi Santoso",
                            "customer_email" to "budi@email.com",
                            "total_amount" to "500.000",
                            "date" to "01 Oktober 2025"
                        )
                        clipboardManager.copyWithInjection(sampleTextWithPlaceholders, injectedData)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📋 Copy dengan Data Injection")
                }
            }

            // Section 3: Share Link
            SectionCard(
                title = "3. Share Link",
                description = "Share URL ke aplikasi lain"
            ) {
                Button(
                    onClick = {
                        shareManager.shareText("https://instagram.com/tripkeun")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🔗 Share Link Instagram")
                }
            }

            // Section 4: Share PDF Simple
            SectionCard(
                title = "4. Share PDF Sederhana",
                description = "Generate dan share PDF dari data halaman"
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            isLoadingSharePdfSimple = true
                            withContext(Dispatchers.IO) {
                                try {
                                    val pdfFile = pdfGenerator.generateSimplePdf(
                                        title = "Dokumen Sederhana",
                                        content = sampleText
                                    )
                                    withContext(Dispatchers.Main) {
                                        shareManager.sharePdf(pdfFile)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            isLoadingSharePdfSimple = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoadingSharePdfSimple
                ) {
                    Text(if (isLoadingSharePdfSimple) "⏳ Processing..." else "📤 Share PDF Sederhana")
                }
            }

            // Section 5: Share PDF Custom
            SectionCard(
                title = "5. Share PDF Custom",
                description = "Generate PDF dengan modifikasi tampilan"
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            isLoadingSharePdfCustom = true
                            withContext(Dispatchers.IO) {
                                try {
                                    val sections = listOf(
                                        PdfSection(
                                            title = "Pendahuluan",
                                            content = "Ini adalah dokumen PDF dengan tampilan yang telah dimodifikasi. Format lebih rapi dan terstruktur."
                                        ),
                                        PdfSection(
                                            title = "Konten Utama",
                                            content = sampleText
                                        ),
                                        PdfSection(
                                            title = "Data Tambahan",
                                            content = "Invoice: INV-2024-001\nNama: Budi Santoso\nEmail: budi@email.com\nTotal: Rp 500.000\nTanggal: 01 Oktober 2025"
                                        ),
                                        PdfSection(
                                            title = "Kesimpulan",
                                            content = "Terima kasih telah menggunakan aplikasi Copy Share Download. Aplikasi ini memudahkan Anda dalam mengelola dokumen."
                                        )
                                    )

                                    val pdfFile = pdfGenerator.generateCustomPdf(
                                        title = "Dokumen Custom dengan Modifikasi",
                                        sections = sections
                                    )

                                    withContext(Dispatchers.Main) {
                                        shareManager.sharePdf(pdfFile)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            isLoadingSharePdfCustom = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoadingSharePdfCustom
                ) {
                    Text(if (isLoadingSharePdfCustom) "⏳ Processing..." else "📤 Share PDF Custom")
                }
            }

            // Section 6: Download PDF Simple
            SectionCard(
                title = "6. Download PDF Sederhana",
                description = "Download PDF dari data halaman"
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            isLoadingDownloadPdfSimple = true
                            withContext(Dispatchers.IO) {
                                try {
                                    pdfGenerator.generateSimplePdf(
                                        title = "Dokumen Download Sederhana",
                                        content = sampleText,
                                        fileName = "simple_download.pdf"
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            isLoadingDownloadPdfSimple = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoadingDownloadPdfSimple
                ) {
                    Text(if (isLoadingDownloadPdfSimple) "⏳ Processing..." else "💾 Download PDF Sederhana")
                }
            }

            // Section 7: Download PDF Custom
            SectionCard(
                title = "7. Download PDF Custom",
                description = "Download PDF dengan modifikasi tampilan"
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            isLoadingDownloadPdfCustom = true
                            withContext(Dispatchers.IO) {
                                try {
                                    val sections = listOf(
                                        PdfSection(
                                            title = "Laporan Penjualan",
                                            content = "Berikut adalah ringkasan penjualan bulan Oktober 2025"
                                        ),
                                        PdfSection(
                                            title = "Detail Transaksi",
                                            content = """
                                                Produk A: 50 unit x Rp 100.000 = Rp 5.000.000
                                                Produk B: 30 unit x Rp 150.000 = Rp 4.500.000
                                                Produk C: 20 unit x Rp 200.000 = Rp 4.000.000
                                            """.trimIndent()
                                        ),
                                        PdfSection(
                                            title = "Total",
                                            content = "Total Penjualan: Rp 13.500.000"
                                        ),
                                        PdfSection(
                                            title = "Catatan",
                                            content = "Data ini telah diverifikasi dan siap untuk dilaporkan. Dokumen ini dibuat secara otomatis oleh sistem."
                                        )
                                    )

                                    pdfGenerator.generateCustomPdf(
                                        title = "Laporan Custom - Oktober 2025",
                                        sections = sections,
                                        fileName = "custom_download_report.pdf"
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            isLoadingDownloadPdfCustom = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoadingDownloadPdfCustom
                ) {
                    Text(if (isLoadingDownloadPdfCustom) "⏳ Processing..." else "💾 Download PDF Custom")
                }
            }

            // Section 8: Share Image Custom
            SectionCard(
                title = "8. Share Image Custom",
                description = "Generate dan share Image dengan modifikasi tampilan"
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            isLoadingShareImage = true
                            withContext(Dispatchers.IO) {
                                try {
                                    val sections = listOf(
                                        ImageSection(
                                            title = "Informasi Produk",
                                            content = "Produk unggulan kami hadir dengan kualitas terbaik dan harga terjangkau. Dapatkan penawaran spesial hari ini!"
                                        ),
                                        ImageSection(
                                            title = "Konten Promosi",
                                            content = sampleText
                                        ),
                                        ImageSection(
                                            title = "Data Penjualan",
                                            content = "Total Penjualan Bulan Ini: Rp 13.500.000 | Target Tercapai: 135% | Jumlah Transaksi: 127 | Rating: 4.8/5.0"
                                        )
                                    )

                                    val imageFile = imageGenerator.generateImage(
                                        title = "Laporan Visual - Oktober 2025",
                                        sections = sections
                                    )

                                    withContext(Dispatchers.Main) {
                                        shareManager.shareImage(imageFile)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            isLoadingShareImage = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoadingShareImage
                ) {
                    Text(if (isLoadingShareImage) "⏳ Processing..." else "🖼️ Share Image Custom")
                }
            }

            // Section 9: Download Image Custom
            SectionCard(
                title = "9. Download Image Custom",
                description = "Download Image dengan modifikasi tampilan"
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            isLoadingDownloadImage = true
                            withContext(Dispatchers.IO) {
                                try {
                                    val sections = listOf(
                                        ImageSection(
                                            title = "Laporan Marketing",
                                            content = "Campaign bulan ini mencapai target dengan engagement rate 8.5% dan conversion rate 3.2%"
                                        ),
                                        ImageSection(
                                            title = "Data Performa",
                                            content = """
                                                Impressions: 125,000
                                                Clicks: 10,625
                                                Conversions: 340
                                                Revenue: Rp 17,000,000
                                            """.trimIndent()
                                        ),
                                        ImageSection(
                                            title = "Kesimpulan",
                                            content = "Performance bulan ini sangat memuaskan dengan ROI 340%. Target bulan depan dinaikkan menjadi Rp 20,000,000."
                                        )
                                    )

                                    imageGenerator.generateImage(
                                        title = "Marketing Report - Oktober 2025",
                                        sections = sections,
                                        fileName = "marketing_report.png"
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            isLoadingDownloadImage = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoadingDownloadImage
                ) {
                    Text(if (isLoadingDownloadImage) "⏳ Processing..." else "💾 Download Image Custom")
                }
            }

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "ℹ️ Informasi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• File PDF akan disimpan di folder Documents/PDFs (Android 10+) atau Downloads/CopyShareDownload\n" +
                                "• File Image akan disimpan di folder Pictures/Images (Android 10+) atau Pictures/CopyShareDownload\n" +
                                "• Image menggunakan resolusi tinggi (1080px) dengan ukuran menyesuaikan konten\n" +
                                "• Text yang di-copy akan tersimpan di clipboard dan siap di-paste\n" +
                                "• Share akan membuka aplikasi lain untuk berbagi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}