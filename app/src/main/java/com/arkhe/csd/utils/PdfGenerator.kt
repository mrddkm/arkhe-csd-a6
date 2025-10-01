@file:Suppress("SpellCheckingInspection")

package com.arkhe.csd.utils

import android.content.Context
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Chunk
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfGenerator(private val context: Context) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())

    fun generateSimplePdf(
        title: String,
        content: String,
        fileName: String? = null
    ): File {
        val document = Document()
        val timestamp = dateFormat.format(Date())
        val pdfFileName = fileName ?: "document_$timestamp.pdf"
        val pdfFile = createPdfFile(pdfFileName)

        try {
            PdfWriter.getInstance(document, FileOutputStream(pdfFile))
            document.open()

            // Title
            val titleFont = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD)
            val titleParagraph = Paragraph(title, titleFont)
            titleParagraph.alignment = Element.ALIGN_CENTER
            titleParagraph.spacingAfter = 20f
            document.add(titleParagraph)

            // Content
            val contentFont = Font(Font.FontFamily.HELVETICA, 12f, Font.NORMAL)
            val contentParagraph = Paragraph(content, contentFont)
            contentParagraph.alignment = Element.ALIGN_LEFT
            document.add(contentParagraph)

            document.close()

            // Copy to public directory for download (Android 10+)
            if (fileName != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                copyToDownloads(pdfFile, pdfFileName)
            }

            Toast.makeText(context, "PDF berhasil dibuat! 📄", Toast.LENGTH_SHORT).show()
            return pdfFile

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            throw e
        }
    }

    fun generateCustomPdf(
        title: String,
        sections: List<PdfSection>,
        fileName: String? = null
    ): File {
        val document = Document()
        val timestamp = dateFormat.format(Date())
        val pdfFileName = fileName ?: "custom_document_$timestamp.pdf"
        val pdfFile = createPdfFile(pdfFileName)

        try {
            PdfWriter.getInstance(document, FileOutputStream(pdfFile))
            document.open()

            // Main Title
            val titleFont = Font(Font.FontFamily.HELVETICA, 20f, Font.BOLD, BaseColor.BLUE)
            val titleParagraph = Paragraph(title, titleFont)
            titleParagraph.alignment = Element.ALIGN_CENTER
            titleParagraph.spacingAfter = 30f
            document.add(titleParagraph)

            // Add timestamp
            val timestampFont = Font(Font.FontFamily.HELVETICA, 10f, Font.ITALIC, BaseColor.GRAY)
            val timestampParagraph = Paragraph(
                "Generated: ${
                    SimpleDateFormat(
                        "dd MMMM yyyy HH:mm",
                        Locale.getDefault()
                    ).format(Date())
                }", timestampFont
            )
            timestampParagraph.alignment = Element.ALIGN_CENTER
            timestampParagraph.spacingAfter = 20f
            document.add(timestampParagraph)

            // Add sections
            sections.forEach { section ->
                // Section Title
                val sectionTitleFont = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD)
                val sectionTitleParagraph = Paragraph(section.title, sectionTitleFont)
                sectionTitleParagraph.spacingBefore = 15f
                sectionTitleParagraph.spacingAfter = 10f
                document.add(sectionTitleParagraph)

                // Section Content
                val sectionContentFont = Font(Font.FontFamily.HELVETICA, 11f, Font.NORMAL)
                val sectionContentParagraph = Paragraph(section.content, sectionContentFont)
                sectionContentParagraph.spacingAfter = 10f
                document.add(sectionContentParagraph)

                // Add line separator
                if (section != sections.last()) {
                    document.add(Chunk.NEWLINE)
                }
            }

            // Footer
            document.add(Chunk.NEWLINE)
            document.add(Chunk.NEWLINE)
            val footerFont = Font(Font.FontFamily.HELVETICA, 9f, Font.ITALIC, BaseColor.GRAY)
            val footerParagraph = Paragraph("Created with Copy Share Download App", footerFont)
            footerParagraph.alignment = Element.ALIGN_CENTER
            document.add(footerParagraph)

            document.close()

            // Copy to public directory for download (Android 10+)
            if (fileName != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                copyToDownloads(pdfFile, pdfFileName)
            }

            Toast.makeText(context, "Custom PDF berhasil dibuat! 📄✨", Toast.LENGTH_SHORT).show()
            return pdfFile

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            throw e
        }
    }

    private fun createPdfFile(fileName: String): File {
        // For Android 10+ (API 29+), use app-specific directory
        val pdfDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "PDFs")
        } else {
            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            File(downloadsDir, "CopyShareDownload")
        }

        if (!pdfDir.exists()) {
            pdfDir.mkdirs()
        }

        return File(pdfDir, fileName)
    }

    private fun copyToDownloads(sourceFile: File, fileName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/CopyShareDownload")
                }

                val uri = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        sourceFile.inputStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

data class PdfSection(
    val title: String,
    val content: String
)