@file:Suppress("SpellCheckingInspection")

package com.arkhe.csd.utils

import android.content.Context
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt

class ImageGenerator(private val context: Context) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())

    companion object {
        private const val IMAGE_WIDTH = 1080 // High resolution width
        private const val PADDING = 60
        private const val TITLE_SIZE = 64f
        private const val SECTION_TITLE_SIZE = 48f
        private const val CONTENT_SIZE = 36f
        private const val LINE_SPACING = 1.5f
    }

    fun generateImage(
        title: String,
        sections: List<ImageSection>,
        fileName: String? = null
    ): File {
        val timestamp = dateFormat.format(Date())
        val imageFileName = fileName ?: "image_$timestamp.png"
        val imageFile = createImageFile(imageFileName)

        try {
            // Calculate required height based on content
            val requiredHeight = calculateRequiredHeight(title, sections)

            // Create bitmap with calculated dimensions
            val bitmap = createBitmap(IMAGE_WIDTH, requiredHeight)
            val canvas = Canvas(bitmap)

            // Draw background gradient
            drawGradientBackground(canvas)

            // Initialize paints
            val titlePaint = createTextPaint(TITLE_SIZE, Color.WHITE, true)
            val sectionTitlePaint =
                createTextPaint(SECTION_TITLE_SIZE, "#FFD700".toColorInt(), true)
            val contentPaint = createTextPaint(CONTENT_SIZE, Color.WHITE, false)
            val timestampPaint = createTextPaint(28f, "#B0BEC5".toColorInt(), false)

            var currentY = PADDING.toFloat()

            // Draw main title
            currentY = drawTitle(canvas, title, titlePaint, currentY)

            // Draw timestamp
            currentY = drawTimestamp(canvas, timestampPaint, currentY)

            // Draw decorative line
            currentY = drawDecorativeLine(canvas, currentY)

            // Draw sections
            sections.forEach { section ->
                currentY = drawSection(
                    canvas,
                    section,
                    sectionTitlePaint,
                    contentPaint,
                    currentY
                )
            }

            // Draw footer
            drawFooter(canvas, requiredHeight)

            // Save bitmap to file
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            // Copy to public directory for download (Android 10+)
            if (fileName != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                copyToPictures(imageFile, imageFileName)
            }

            bitmap.recycle()

            Toast.makeText(context, "Image berhasil dibuat! 🖼️", Toast.LENGTH_SHORT).show()
            return imageFile

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            throw e
        }
    }

    private fun calculateRequiredHeight(title: String, sections: List<ImageSection>): Int {
        var height = PADDING * 3 // Top padding + bottom padding

        // Title height
        height += 120

        // Timestamp height
        height += 60

        // Decorative line
        height += 40

        // Calculate sections height
        sections.forEach { section ->
            // Section title
            height += 80

            // Section content - calculate wrapped text height
            val lines = wrapText(section.content, IMAGE_WIDTH - (PADDING * 2), CONTENT_SIZE)
            height += (lines.size * CONTENT_SIZE * LINE_SPACING).toInt() + 60
        }

        // Footer
        height += 100

        return height.coerceAtLeast(1920) // Minimum height
    }

    private fun drawGradientBackground(canvas: Canvas) {
        val gradient = LinearGradient(
            0f, 0f, 0f, canvas.height.toFloat(),
            intArrayOf(
                "#1a237e".toColorInt(), // Deep blue
                "#283593".toColorInt(),
                "#303f9f".toColorInt()
            ),
            null,
            Shader.TileMode.CLAMP
        )

        val paint = Paint().apply {
            shader = gradient
        }

        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)

        // Add subtle pattern overlay
        drawPattern(canvas)
    }

    private fun drawPattern(canvas: Canvas) {
        val patternPaint = Paint().apply {
            color = Color.WHITE
            alpha = 10
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        val spacing = 50
        for (i in 0 until canvas.width step spacing) {
            canvas.drawLine(i.toFloat(), 0f, i.toFloat(), canvas.height.toFloat(), patternPaint)
        }

        for (i in 0 until canvas.height step spacing) {
            canvas.drawLine(0f, i.toFloat(), canvas.width.toFloat(), i.toFloat(), patternPaint)
        }
    }

    private fun drawTitle(canvas: Canvas, title: String, paint: Paint, startY: Float): Float {
        val lines = wrapText(title, IMAGE_WIDTH - (PADDING * 2), TITLE_SIZE)
        var y = startY + TITLE_SIZE

        lines.forEach { line ->
            val x = (IMAGE_WIDTH - paint.measureText(line)) / 2
            canvas.drawText(line, x, y, paint)
            y += TITLE_SIZE * LINE_SPACING
        }

        return y + 20
    }

    @Suppress("DEPRECATION")
    private fun drawTimestamp(canvas: Canvas, paint: Paint, startY: Float): Float {
        val timestamp = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(Date())
        val x = (IMAGE_WIDTH - paint.measureText(timestamp)) / 2
        canvas.drawText(timestamp, x, startY + 30, paint)
        return startY + 60
    }

    private fun drawDecorativeLine(canvas: Canvas, startY: Float): Float {
        val linePaint = Paint().apply {
            color = "#FFD700".toColorInt()
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }

        val lineWidth = 300f
        val startX = (IMAGE_WIDTH - lineWidth) / 2
        canvas.drawLine(startX, startY, startX + lineWidth, startY, linePaint)

        return startY + 40
    }

    private fun drawSection(
        canvas: Canvas,
        section: ImageSection,
        titlePaint: Paint,
        contentPaint: Paint,
        startY: Float
    ): Float {
        var y = startY

        // Calculate content height first
        val lines = wrapText(section.content, IMAGE_WIDTH - (PADDING * 2) - 40, CONTENT_SIZE)
        val contentHeight = lines.size * CONTENT_SIZE * LINE_SPACING

        // Draw section background card
        val cardPaint = Paint().apply {
            color = "#1e2a4a".toColorInt()
            alpha = 180
            style = Paint.Style.FILL
        }

        val cardRect = RectF(
            PADDING.toFloat(),
            y - 20,
            (IMAGE_WIDTH - PADDING).toFloat(),
            y + SECTION_TITLE_SIZE + contentHeight + 60
        )

        canvas.drawRoundRect(cardRect, 20f, 20f, cardPaint)

        // Draw section title with icon
        val icon = getIconForSection(section.title)
        canvas.drawText(icon, PADDING + 20f, y + SECTION_TITLE_SIZE, titlePaint)
        canvas.drawText(section.title, PADDING + 80f, y + SECTION_TITLE_SIZE, titlePaint)
        y += SECTION_TITLE_SIZE + 40

        // Draw content with word wrapping
        lines.forEach { line ->
            canvas.drawText(line, PADDING + 40f, y, contentPaint)
            y += CONTENT_SIZE * LINE_SPACING
        }

        return y + 60
    }

    private fun getIconForSection(title: String): String {
        return when {
            title.contains("Pendahuluan", ignoreCase = true) -> "📋"
            title.contains("Konten", ignoreCase = true) -> "📝"
            title.contains("Data", ignoreCase = true) -> "📊"
            title.contains("Informasi", ignoreCase = true) -> "ℹ️"
            title.contains("Laporan", ignoreCase = true) -> "📈"
            title.contains("Transaksi", ignoreCase = true) -> "💳"
            title.contains("Total", ignoreCase = true) -> "💰"
            title.contains("Catatan", ignoreCase = true) -> "📌"
            title.contains("Kesimpulan", ignoreCase = true) -> "✅"
            title.contains("Performa", ignoreCase = true) -> "📊"
            title.contains("Marketing", ignoreCase = true) -> "📢"
            title.contains("Promosi", ignoreCase = true) -> "🎁"
            title.contains("Penjualan", ignoreCase = true) -> "💰"
            else -> "•"
        }
    }

    private fun drawFooter(canvas: Canvas, height: Int) {
        val footerPaint = createTextPaint(24f, "#B0BEC5".toColorInt(), false)
        footerPaint.textAlign = Paint.Align.CENTER

        val footerText = "✨ Created with Copy Share Download App ✨"
        canvas.drawText(
            footerText,
            (IMAGE_WIDTH / 2).toFloat(),
            height - PADDING.toFloat(),
            footerPaint
        )
    }

    private fun createTextPaint(size: Float, color: Int, bold: Boolean): Paint {
        return Paint().apply {
            this.color = color
            textSize = size
            isAntiAlias = true
            typeface =
                if (bold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
        }
    }

    private fun wrapText(text: String, maxWidth: Int, textSize: Float): List<String> {
        val paint = Paint().apply {
            this.textSize = textSize
        }

        val lines = mutableListOf<String>()
        val paragraphs = text.split("\n")

        paragraphs.forEach { paragraph ->
            if (paragraph.trim().isEmpty()) {
                lines.add("")
                return@forEach
            }

            val words = paragraph.split(" ")
            var currentLine = ""

            words.forEach { word ->
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                val width = paint.measureText(testLine)

                if (width > maxWidth) {
                    if (currentLine.isNotEmpty()) {
                        lines.add(currentLine)
                        currentLine = word
                    } else {
                        lines.add(word)
                    }
                } else {
                    currentLine = testLine
                }
            }

            if (currentLine.isNotEmpty()) {
                lines.add(currentLine)
            }
        }

        return lines
    }

    private fun createImageFile(fileName: String): File {
        val imagesDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Images")
        } else {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "CopyShareDownload"
            )
        }

        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }

        return File(imagesDir, fileName)
    }

    private fun copyToPictures(sourceFile: File, fileName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CopyShareDownload")
                }

                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
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

data class ImageSection(
    val title: String,
    val content: String
)