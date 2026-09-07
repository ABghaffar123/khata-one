package com.khatabook.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.khatabook.app.ui.screen.reports.CashBookState
import com.khatabook.app.ui.screen.reports.DateRange
import java.io.File
import java.io.FileWriter

/**
 * ═══════════════════════════════════════════════════════════════════
 * REPORT EXPORTER — PDF and CSV generation for Cash Book reports
 * ═══════════════════════════════════════════════════════════════════
 *
 * Generates:
 *   1. PDF — Formatted report with all sections
 *   2. CSV — Tabular data for Excel/spreadsheet import
 *
 * Both include: date range, opening cash, cash in/out,
 * income breakdown, and business overview.
 */
object ReportExporter {

    // ═══════════════════════════════════════════════════════════════
    // PDF EXPORT
    // ═══════════════════════════════════════════════════════════════

    /**
     * Generate a PDF report and return the file.
     */
    fun generatePdf(context: Context, state: CashBookState, shopName: String): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size

        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // Paints
        val titlePaint = Paint().apply {
            textSize = 20f
            color = Color.parseColor("#1a1a1a")
            isFakeBoldText = true
        }
        val headerPaint = Paint().apply {
            textSize = 14f
            color = Color.parseColor("#ea580c")
            isFakeBoldText = true
        }
        val labelPaint = Paint().apply {
            textSize = 11f
            color = Color.parseColor("#666666")
        }
        val valuePaint = Paint().apply {
            textSize = 12f
            color = Color.parseColor("#1a1a1a")
            isFakeBoldText = true
        }
        val totalPaint = Paint().apply {
            textSize = 16f
            color = Color.parseColor("#ea580c")
            isFakeBoldText = true
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#dddddd")
            strokeWidth = 1f
        }
        val sectionPaint = Paint().apply {
            textSize = 13f
            color = Color.parseColor("#1a1a1a")
            isFakeBoldText = true
        }

        var y = 40f
        val leftMargin = 40f
        val rightMargin = 555f
        val lineHeight = 20f

        // ── Header ──
        canvas.drawText(shopName, leftMargin, y, titlePaint)
        y += 28f
        canvas.drawText("CASH BOOK REPORT", leftMargin, y, headerPaint)
        y += 25f

        // Date range
        val dateRangeStr = getDateRangeString(state)
        canvas.drawText("Period: $dateRangeStr", leftMargin, y, labelPaint)
        y += 18f
        canvas.drawText("Generated: ${PkDateTime.formatDateTime(PkDateTime.nowUtc())}", leftMargin, y, labelPaint)
        y += 25f

        // Divider
        canvas.drawLine(leftMargin, y, rightMargin, y, linePaint)
        y += 20f

        // ── Opening Cash ──
        canvas.drawText("OPENING CASH", leftMargin, y, sectionPaint)
        y += lineHeight
        canvas.drawText("Rs ${String.format("%,.0f", state.openingCash)}", leftMargin, y, valuePaint)
        y += 25f

        // Divider
        canvas.drawLine(leftMargin, y, rightMargin, y, linePaint)
        y += 20f

        // ── Cash In Hand ──
        canvas.drawText("CASH IN HAND (GALLA BALANCE)", leftMargin, y, sectionPaint)
        y += lineHeight
        canvas.drawText("Rs ${String.format("%,.0f", state.closingCash)}", leftMargin, y, totalPaint)
        y += 25f

        // Cash In vs Cash Out
        canvas.drawText("Cash In:", leftMargin, y, labelPaint)
        canvas.drawText(
            "Rs ${String.format("%,.0f", state.totalCashSales + state.totalPaymentsReceived)}",
            leftMargin + 80f, y, valuePaint
        )
        y += lineHeight
        canvas.drawText("Cash Out:", leftMargin, y, labelPaint)
        canvas.drawText(
            "Rs ${String.format("%,.0f", state.totalExpenses + state.totalPaymentsToSuppliers)}",
            leftMargin + 80f, y, valuePaint
        )
        y += 25f

        // Divider
        canvas.drawLine(leftMargin, y, rightMargin, y, linePaint)
        y += 20f

        // ── Income Breakdown ──
        canvas.drawText("INCOME BREAKDOWN", leftMargin, y, sectionPaint)
        y += lineHeight + 5

        // Two-column layout
        val col1X = leftMargin
        val col2X = leftMargin + 250f

        canvas.drawText("Cash Sales:", col1X, y, labelPaint)
        canvas.drawText("Rs ${String.format("%,.0f", state.totalCashSales)}", col1X + 100f, y, valuePaint)

        canvas.drawText("Payments Received:", col2X, y, labelPaint)
        canvas.drawText("Rs ${String.format("%,.0f", state.totalPaymentsReceived)}", col2X + 130f, y, valuePaint)
        y += lineHeight

        canvas.drawText("Credit Given:", col1X, y, labelPaint)
        canvas.drawText("Rs ${String.format("%,.0f", state.totalCreditGiven)}", col1X + 100f, y, valuePaint)

        canvas.drawText("Expenses:", col2X, y, labelPaint)
        canvas.drawText("Rs ${String.format("%,.0f", state.totalExpenses)}", col2X + 130f, y, valuePaint)
        y += lineHeight

        canvas.drawText("Purchases:", col1X, y, labelPaint)
        canvas.drawText("Rs ${String.format("%,.0f", state.totalPurchases)}", col1X + 100f, y, valuePaint)

        canvas.drawText("Supplier Payments:", col2X, y, labelPaint)
        canvas.drawText("Rs ${String.format("%,.0f", state.totalPaymentsToSuppliers)}", col2X + 130f, y, valuePaint)
        y += 25f

        // Divider
        canvas.drawLine(leftMargin, y, rightMargin, y, linePaint)
        y += 20f

        // ── Business Overview ──
        canvas.drawText("BUSINESS OVERVIEW", leftMargin, y, sectionPaint)
        y += lineHeight + 5

        canvas.drawText("Stock Value:", col1X, y, labelPaint)
        canvas.drawText("Rs ${String.format("%,.0f", state.stockValue)}", col1X + 100f, y, valuePaint)

        canvas.drawText("Potential Revenue:", col2X, y, labelPaint)
        canvas.drawText("Rs ${String.format("%,.0f", state.potentialRevenue)}", col2X + 130f, y, valuePaint)
        y += lineHeight

        canvas.drawText("Estimated Profit:", col1X, y, labelPaint)
        canvas.drawText(
            "Rs ${String.format("%,.0f", state.estimatedProfit)}",
            col1X + 100f, y, valuePaint
        )
        y += lineHeight

        canvas.drawText("Total Transactions:", col1X, y, labelPaint)
        canvas.drawText("${state.totalTransactionCount}", col1X + 130f, y, valuePaint)
        y += 30f

        // Divider
        canvas.drawLine(leftMargin, y, rightMargin, y, linePaint)
        y += 15f

        // Footer
        val footerPaint = Paint().apply {
            textSize = 9f
            color = Color.parseColor("#999999")
        }
        canvas.drawText("Generated by Khata One | ${PkDateTime.formatDateTime(PkDateTime.nowUtc())}", leftMargin, y, footerPaint)

        document.finishPage(page)

        // Save file
        val fileName = "KhataOne_Report_${PkDateTime.formatShortDate(PkDateTime.nowUtc()).replace("/", "-")}.pdf"
        val file = File(context.cacheDir, fileName)
        file.outputStream().use { out ->
            document.writeTo(out)
        }
        document.close()

        return file
    }


    // ═══════════════════════════════════════════════════════════════
    // CSV EXPORT
    // ═══════════════════════════════════════════════════════════════

    /**
     * Generate a CSV report and return the file.
     */
    fun generateCsv(context: Context, state: CashBookState, shopName: String): File {
        val fileName = "KhataOne_Report_${PkDateTime.formatShortDate(PkDateTime.nowUtc()).replace("/", "-")}.csv"
        val file = File(context.cacheDir, fileName)

        FileWriter(file).use { writer ->
            // Header section
            writer.appendLine("KHATA ONE - CASH BOOK REPORT")
            writer.appendLine("Shop,$shopName")
            writer.appendLine("Period,${getDateRangeString(state)}")
            writer.appendLine("Generated,${PkDateTime.formatDateTime(PkDateTime.nowUtc())}")
            writer.appendLine()

            // Opening Cash
            writer.appendLine("SECTION,ITEM,AMOUNT (PKR)")
            writer.appendLine("Opening Cash,Opening Cash,${String.format("%.2f", state.openingCash)}")
            writer.appendLine()

            // Cash In Hand
            writer.appendLine("Cash In Hand,Closing Cash,${String.format("%.2f", state.closingCash)}")
            writer.appendLine("Cash In Hand,Cash In,${String.format("%.2f", state.totalCashSales + state.totalPaymentsReceived)}")
            writer.appendLine("Cash In Hand,Cash Out,${String.format("%.2f", state.totalExpenses + state.totalPaymentsToSuppliers)}")
            writer.appendLine()

            // Income Breakdown
            writer.appendLine("Income Breakdown,Cash Sales,${String.format("%.2f", state.totalCashSales)}")
            writer.appendLine("Income Breakdown,Payments Received,${String.format("%.2f", state.totalPaymentsReceived)}")
            writer.appendLine("Income Breakdown,Credit Given,${String.format("%.2f", state.totalCreditGiven)}")
            writer.appendLine("Income Breakdown,Expenses,${String.format("%.2f", state.totalExpenses)}")
            writer.appendLine("Income Breakdown,Purchases,${String.format("%.2f", state.totalPurchases)}")
            writer.appendLine("Income Breakdown,Supplier Payments,${String.format("%.2f", state.totalPaymentsToSuppliers)}")
            writer.appendLine()

            // Business Overview
            writer.appendLine("Business Overview,Stock Value,${String.format("%.2f", state.stockValue)}")
            writer.appendLine("Business Overview,Potential Revenue,${String.format("%.2f", state.potentialRevenue)}")
            writer.appendLine("Business Overview,Estimated Profit,${String.format("%.2f", state.estimatedProfit)}")
            writer.appendLine("Business Overview,Total Transactions,${state.totalTransactionCount}")
            writer.appendLine()

            // Summary
            writer.appendLine("SUMMARY")
            writer.appendLine("Opening Cash,${String.format("%.2f", state.openingCash)}")
            writer.appendLine("Cash In,${String.format("%.2f", state.totalCashSales + state.totalPaymentsReceived)}")
            writer.appendLine("Cash Out,${String.format("%.2f", state.totalExpenses + state.totalPaymentsToSuppliers)}")
            writer.appendLine("Closing Cash,${String.format("%.2f", state.closingCash)}")
            writer.appendLine("Estimated Profit,${String.format("%.2f", state.estimatedProfit)}")
        }

        return file
    }


    // ═══════════════════════════════════════════════════════════════
    // SHARE INTENTS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Create a share intent for a file (PDF or CSV).
     */
    fun createShareIntent(context: Context, file: File, mimeType: String): Intent {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Khata One Report")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * Create an open intent for a file (PDF or CSV).
     */
    fun createOpenIntent(context: Context, file: File, mimeType: String): Intent {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }


    // ═══════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════

    private fun getDateRangeString(state: CashBookState): String {
        return when (state.dateRange) {
            DateRange.TODAY -> "Today (${PkDateTime.formatDisplayDate(PkDateTime.startOfPktToday())})"
            DateRange.THIS_WEEK -> "This Week"
            DateRange.THIS_MONTH -> "This Month"
            DateRange.CUSTOM -> {
                val start = state.customStartDate?.let { PkDateTime.formatDisplayDate(it) } ?: "?"
                val end = state.customEndDate?.let { PkDateTime.formatDisplayDate(it) } ?: "?"
                "$start — $end"
            }
        }
    }
}
