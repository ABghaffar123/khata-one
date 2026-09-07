package com.khatabook.app.notification

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.khatabook.app.util.toCurrency

/**
 * Manages daily summary scheduling, download history, and file generation.
 */
object DailySummaryManager {

    private const val PREFS_NAME = "khata_prefs"
    private const val KEY_SUMMARY_HOUR = "summary_hour"
    private const val KEY_SUMMARY_MINUTE = "summary_minute"
    private const val KEY_SUMMARY_ENABLED = "summary_enabled"
    private const val KEY_DOWNLOADED_SUMMARIES = "downloaded_summaries"

    // ═══════════════════ SCHEDULING ═══════════════════

    fun isSummaryEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SUMMARY_ENABLED, true)
    }

    fun setSummaryEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_SUMMARY_ENABLED, enabled).apply()
        if (enabled) {
            DailySummaryReceiver.scheduleAlarm(context)
        } else {
            DailySummaryReceiver.cancelAlarm(context)
        }
    }

    fun getSummaryTime(context: Context): Pair<Int, Int> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return Pair(
            prefs.getInt(KEY_SUMMARY_HOUR, 21),
            prefs.getInt(KEY_SUMMARY_MINUTE, 0)
        )
    }

    fun setSummaryTime(context: Context, hour: Int, minute: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_SUMMARY_HOUR, hour)
            .putInt(KEY_SUMMARY_MINUTE, minute)
            .apply()
        DailySummaryReceiver.scheduleAlarm(context)
    }

    fun getFormattedTime(context: Context): String {
        val (hour, minute) = getSummaryTime(context)
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
    }

    // ═══════════════════ HISTORY ═══════════════════

    data class SummaryRecord(
        val dateKey: String,  // e.g. "2024-01-15"
        val displayDate: String, // e.g. "15 Jan 2024"
        val filePath: String,
        val fileType: String  // "pdf" or "png"
    )

    fun markSummaryDownloaded(context: Context, dateKey: String, filePath: String, fileType: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getStringSet(KEY_DOWNLOADED_SUMMARIES, emptySet()) ?: emptySet()
        val record = "$dateKey|$filePath|$fileType"
        prefs.edit().putStringSet(KEY_DOWNLOADED_SUMMARIES, existing + record).apply()
    }

    fun isSummaryDownloadedToday(context: Context): Boolean {
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getStringSet(KEY_DOWNLOADED_SUMMARIES, emptySet()) ?: emptySet()
        return existing.any { it.startsWith(todayKey) }
    }

    fun isPastSummaryTime(context: Context): Boolean {
        val (hour, minute) = getSummaryTime(context)
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }
        return now.after(target)
    }

    fun getDownloadedSummaries(context: Context): List<SummaryRecord> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getStringSet(KEY_DOWNLOADED_SUMMARIES, emptySet()) ?: emptySet()
        return existing.mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size == 3) {
                SummaryRecord(
                    dateKey = parts[0],
                    displayDate = formatDateDisplay(parts[0]),
                    filePath = parts[1],
                    fileType = parts[2]
                )
            } else null
        }.sortedByDescending { it.dateKey }.take(7) // Last 7 days
    }

    private fun formatDateDisplay(dateKey: String): String {
        return try {
            val input = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val output = SimpleDateFormat("dd MMM yyyy", Locale.US)
            output.format(input.parse(dateKey) ?: Date())
        } catch (e: Exception) {
            dateKey
        }
    }

    // ═══════════════════ PDF GENERATION ═══════════════════

    fun generatePdf(
        context: Context,
        date: String,
        openingCash: Double,
        cashIn: Double,
        cashOut: Double,
        cashInHand: Double,
        cashSales: Double,
        paymentsReceived: Double,
        creditGiven: Double,
        expenses: Double
    ): File? {
        return try {
            val document = PdfDocument()

            // Page 1: Daily Summary
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.parseColor("#FF6B35")
                textSize = 24f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val subtitlePaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 14f
                isAntiAlias = true
            }
            val labelPaint = Paint().apply {
                color = Color.GRAY
                textSize = 12f
                isAntiAlias = true
            }
            val valuePaint = Paint().apply {
                color = Color.BLACK
                textSize = 14f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val greenPaint = Paint().apply {
                color = Color.parseColor("#4CAF50")
                textSize = 14f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val redPaint = Paint().apply {
                color = Color.parseColor("#F44336")
                textSize = 14f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val linePaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 1f
            }

            var y = 60f

            // Title
            canvas.drawText("Khata One", 40f, y, titlePaint)
            y += 30f
            canvas.drawText("Daily Summary", 40f, y, subtitlePaint)
            y += 25f
            canvas.drawText("Date: $date", 40f, y, subtitlePaint)
            y += 40f

            // Divider
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 30f

            // Opening Cash
            canvas.drawText("Opening Cash", 40f, y, labelPaint)
            y += 18f
            canvas.drawText(openingCash.toCurrency(), 40f, y, valuePaint)
            y += 35f

            // Cash In / Cash Out
            canvas.drawText("Cash In", 40f, y, labelPaint)
            canvas.drawText("Cash Out", 320f, y, labelPaint)
            y += 18f
            canvas.drawText(cashIn.toCurrency(), 40f, y, greenPaint)
            canvas.drawText(cashOut.toCurrency(), 320f, y, redPaint)
            y += 35f

            // Divider
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 25f

            // Cash in Hand
            canvas.drawText("Cash in Hand", 40f, y, labelPaint)
            y += 22f
            val handPaint = if (cashInHand >= 0) greenPaint else redPaint
            canvas.drawText(cashInHand.toCurrency(), 40f, y, handPaint)
            y += 45f

            // Divider
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 25f

            // Income Breakdown
            canvas.drawText("Income Breakdown", 40f, y, subtitlePaint)
            y += 30f

            canvas.drawText("Cash Sales", 40f, y, labelPaint)
            canvas.drawText(cashSales.toCurrency(), 320f, y, valuePaint)
            y += 25f

            canvas.drawText("Payments Received", 40f, y, labelPaint)
            canvas.drawText(paymentsReceived.toCurrency(), 320f, y, valuePaint)
            y += 25f

            canvas.drawText("Credit Given", 40f, y, labelPaint)
            canvas.drawText(creditGiven.toCurrency(), 320f, y, redPaint)
            y += 25f

            canvas.drawText("Expenses", 40f, y, labelPaint)
            canvas.drawText(expenses.toCurrency(), 320f, y, redPaint)
            y += 50f

            // Footer
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 20f
            val footerPaint = Paint().apply {
                color = Color.GRAY
                textSize = 10f
                isAntiAlias = true
            }
            canvas.drawText("Generated by Khata One • $date", 40f, y, footerPaint)

            document.finishPage(page)

            // Save to app-private files directory
            val dir = File(context.filesDir, "summaries")
            if (!dir.exists()) dir.mkdirs()
            val fileName = "KhataOne_Summary_${date.replace("/", "-")}.pdf"
            val file = File(dir, fileName)
            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ═══════════════════ IMAGE GENERATION ═══════════════════

    fun generateImage(
        context: Context,
        date: String,
        openingCash: Double,
        cashIn: Double,
        cashOut: Double,
        cashInHand: Double,
        cashSales: Double,
        paymentsReceived: Double,
        creditGiven: Double,
        expenses: Double
    ): File? {
        return try {
            val width = 800
            val height = 1200
            val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Background
            canvas.drawColor(Color.WHITE)

            val titlePaint = Paint().apply {
                color = Color.parseColor("#FF6B35")
                textSize = 36f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val subtitlePaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 22f
                isAntiAlias = true
            }
            val labelPaint = Paint().apply {
                color = Color.GRAY
                textSize = 18f
                isAntiAlias = true
            }
            val valuePaint = Paint().apply {
                color = Color.BLACK
                textSize = 22f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val greenPaint = Paint().apply {
                color = Color.parseColor("#4CAF50")
                textSize = 22f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val redPaint = Paint().apply {
                color = Color.parseColor("#F44336")
                textSize = 22f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val linePaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 2f
            }

            var y = 80f

            canvas.drawText("Khata One", 50f, y, titlePaint)
            y += 45f
            canvas.drawText("Daily Summary", 50f, y, subtitlePaint)
            y += 35f
            canvas.drawText("Date: $date", 50f, y, subtitlePaint)
            y += 50f

            canvas.drawLine(50f, y, width - 50f, y, linePaint)
            y += 40f

            canvas.drawText("Opening Cash", 50f, y, labelPaint)
            y += 28f
            canvas.drawText(openingCash.toCurrency(), 50f, y, valuePaint)
            y += 50f

            canvas.drawText("Cash In", 50f, y, labelPaint)
            canvas.drawText("Cash Out", width / 2f, y, labelPaint)
            y += 28f
            canvas.drawText(cashIn.toCurrency(), 50f, y, greenPaint)
            canvas.drawText(cashOut.toCurrency(), width / 2f, y, redPaint)
            y += 50f

            canvas.drawLine(50f, y, width - 50f, y, linePaint)
            y += 35f

            canvas.drawText("Cash in Hand", 50f, y, labelPaint)
            y += 32f
            val handPaint = if (cashInHand >= 0) greenPaint else redPaint
            canvas.drawText(cashInHand.toCurrency(), 50f, y, handPaint)
            y += 60f

            canvas.drawLine(50f, y, width - 50f, y, linePaint)
            y += 35f

            canvas.drawText("Income Breakdown", 50f, y, subtitlePaint)
            y += 40f

            canvas.drawText("Cash Sales", 50f, y, labelPaint)
            canvas.drawText(cashSales.toCurrency(), width / 2f, y, valuePaint)
            y += 35f

            canvas.drawText("Payments Received", 50f, y, labelPaint)
            canvas.drawText(paymentsReceived.toCurrency(), width / 2f, y, valuePaint)
            y += 35f

            canvas.drawText("Credit Given", 50f, y, labelPaint)
            canvas.drawText(creditGiven.toCurrency(), width / 2f, y, redPaint)
            y += 35f

            canvas.drawText("Expenses", 50f, y, labelPaint)
            canvas.drawText(expenses.toCurrency(), width / 2f, y, redPaint)
            y += 70f

            canvas.drawLine(50f, y, width - 50f, y, linePaint)
            y += 30f
            val footerPaint = Paint().apply {
                color = Color.GRAY
                textSize = 14f
                isAntiAlias = true
            }
            canvas.drawText("Generated by Khata One • $date", 50f, y, footerPaint)

            // Save to app-private files directory
            val dir = File(context.filesDir, "summaries")
            if (!dir.exists()) dir.mkdirs()
            val fileName = "KhataOne_Summary_${date.replace("/", "-")}.png"
            val file = File(dir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
            }
            bitmap.recycle()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
