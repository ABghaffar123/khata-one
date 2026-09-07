package com.khatabook.app.ui.screen.invoice

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.print.PdfDocument
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khatabook.app.data.entity.InvoiceItemEntity
import com.khatabook.app.data.entity.TransactionEntity
import com.khatabook.app.ui.responsive.*
import com.khatabook.app.ui.theme.ErrorRed
import com.khatabook.app.ui.theme.KhataGradientPresets
import com.khatabook.app.ui.theme.SuccessGreen
import com.khatabook.app.ui.language.LocalStrings
import com.khatabook.app.util.PkDateTime
import com.khatabook.app.util.toCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicePreviewScreen(
    onNavigateBack: () -> Unit,
    viewModel: InvoicePreviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val spacing = LocalResponsiveSpacing.current
    val typography = LocalResponsiveTypography.current
    val strings = LocalStrings.current
    val gradientTheme = KhataGradientPresets.FireOrange

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.invoice,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = typography.h2
                        ),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            // Bottom action bar
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Share button (WhatsApp)
                    OutlinedButton(
                        onClick = {
                            uiState.invoiceText?.let { text ->
                                shareViaWhatsApp(context, uiState.transaction?.customerPhone, text)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = uiState.transaction != null
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.share)
                    }

                    // Print button
                    Button(
                        onClick = {
                            uiState.invoiceText?.let { text ->
                                printInvoice(context, uiState.transaction, uiState.items, uiState.shopName, text)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = gradientTheme.gradientStart
                        ),
                        enabled = uiState.transaction != null
                    ) {
                        Icon(
                            Icons.Default.Print,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.print, color = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.error ?: strings.error,
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            else -> {
                // Invoice preview content
                InvoicePreviewContent(
                    transaction = uiState.transaction!!,
                    items = uiState.items,
                    shopName = uiState.shopName,
                    spacing = spacing,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun InvoicePreviewContent(
    transaction: TransactionEntity,
    items: List<InvoiceItemEntity>,
    shopName: String,
    spacing: ResponsiveSpacing,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val gradientTheme = KhataGradientPresets.FireOrange

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(spacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Invoice card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = shopName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.invoice.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = gradientTheme.gradientStart,
                        fontWeight = FontWeight.Bold
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Invoice details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = strings.invoiceNumber,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "INV-${transaction.id}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = strings.date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = PkDateTime.formatDisplayDate(transaction.date),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Customer info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = strings.customer,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = transaction.customerName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (transaction.customerPhone.isNotBlank()) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = strings.phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = transaction.customerPhone,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Items table
                if (items.isNotEmpty()) {
                    // Table header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.item,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.weight(3f)
                        )
                        Text(
                            text = strings.qty,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1.5f),
                            textAlign = TextAlign.End
                        )
                        Text(
                            text = strings.price,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.weight(2f),
                            textAlign = TextAlign.End
                        )
                        Text(
                            text = strings.total,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.weight(2f),
                            textAlign = TextAlign.End
                        )
                    }

                    // Items
                    items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item.productName,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(3f),
                                maxLines = 1
                            )
                            Text(
                                text = "${item.quantity} ${item.unit}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1.5f),
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = item.unitPrice.toCurrency(),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(2f),
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = item.totalPrice.toCurrency(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(2f),
                                textAlign = TextAlign.End
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Items subtotal
                    val itemsTotal = items.sumOf { it.totalPrice }
                    if (itemsTotal != transaction.amount) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = strings.itemsSubtotal,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = itemsTotal.toCurrency(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        val adjustValue = transaction.amount - itemsTotal
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = strings.adjustment,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = adjustValue.toCurrency(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (adjustValue > 0) SuccessGreen else ErrorRed
                            )
                        }
                    }
                } else {
                    // No items — simple transaction
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Type",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = transaction.type.name.replace("_", " ").lowercase()
                                    .replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = strings.amount,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = transaction.amount.toCurrency(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    transaction.description?.let { desc ->
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {                            Text(
                                text = strings.totalAmount,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = transaction.amount.toCurrency(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = gradientTheme.gradientStart
                    )
                }

                // Payment status
                if (transaction.dueDate != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.dueDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = PkDateTime.formatDisplayDate(transaction.dueDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (PkDateTime.isAfterPktToday(transaction.dueDate)) {
                                ErrorRed
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            }
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Footer
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = strings.thankYou,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = strings.generatedBy,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp)) // Space for bottom bar
    }
}


// ═══════════════════════════════════════════════════════════════════
// SHARE & PRINT FUNCTIONS
// ═══════════════════════════════════════════════════════════════════

private fun shareViaWhatsApp(context: Context, phone: String?, message: String) {
    try {
        val normalizedPhone = if (!phone.isNullOrBlank()) {
            if (phone.startsWith("+")) phone
            else if (phone.startsWith("0")) "+92${phone.substring(1)}"
            else phone
        } else null

        val url = if (normalizedPhone != null) {
            "https://wa.me/$normalizedPhone?text=${Uri.encode(message)}"
        } else {
            "https://wa.me/?text=${Uri.encode(message)}"
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback: share via general share sheet
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                putExtra(Intent.EXTRA_SUBJECT, "Invoice")
            }
            context.startActivity(Intent.createChooser(intent, "Share Invoice"))
        } catch (e2: Exception) {
            Toast.makeText(context, "No app found to share", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun printInvoice(
    context: Context,
    transaction: TransactionEntity?,
    items: List<InvoiceItemEntity>,
    shopName: String,
    invoiceText: String
) {
    if (transaction == null) return

    try {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "Khata One Invoice - INV-${transaction.id}"

        // Build HTML for printing
        val html = buildPrintHtml(transaction, items, shopName)

        val printAdapter = object : PrintDocumentAdapter() {
            private var webView: WebView? = null

            override fun onStart() {
                webView = WebView(context).apply {
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            // Page loaded, ready to print
                        }
                    }
                    loadDataWithBaseURL(null, html, "text/HTML", "UTF-8", null)
                }
                onStartFinished()
            }

            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes,
                cancellationSignal: android.os.CancellationSignal?,
                layoutResultCallback: LayoutResultCallback?,
                extras: android.os.Bundle?
            ) {
                val info = PrintDocumentInfo.Builder(jobName)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()
                layoutResultCallback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out android.graphics.pdf.PdfDocument.Page>,
                destination: android.os.ParcelFileDescriptor?,
                cancellationSignal: android.os.CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                callback?.onWriteFinished(arrayOf(android.print.PrintDocumentInfo.PageRange.ALL_PAGES))
            }

            override fun onFinish() {
                webView?.destroy()
            }
        }

        printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
    } catch (e: Exception) {
        Toast.makeText(context, "Print not available", Toast.LENGTH_SHORT).show()
    }
}

private fun buildPrintHtml(
    transaction: TransactionEntity,
    items: List<InvoiceItemEntity>,
    shopName: String
): String {
    val dateStr = PkDateTime.formatDisplayDate(transaction.date)

    val itemsHtml = if (items.isNotEmpty()) {
        val rows = items.joinToString("\n") { item ->
            """
            <tr>
                <td>${item.productName}</td>
                <td style="text-align:center">${item.quantity} ${item.unit}</td>
                <td style="text-align:right">${item.unitPrice.toCurrency()}</td>
                <td style="text-align:right;font-weight:bold">${item.totalPrice.toCurrency()}</td>
            </tr>
            """.trimIndent()
        }

        val itemsTotal = items.sumOf { it.totalPrice }
        val adjustmentHtml = if (itemsTotal != transaction.amount) {
            """
            <tr>
                <td colspan="3" style="text-align:right;color:#666">Adjustment</td>
                <td style="text-align:right;color:${if (transaction.amount > itemsTotal) "#16a34a" else "#dc2626"}">${(transaction.amount - itemsTotal).toCurrency()}</td>
            </tr>
            """.trimIndent()
        } else ""

        """
        <table style="width:100%;border-collapse:collapse;margin:16px 0">
            <thead>
                <tr style="border-bottom:1px solid #ddd">
                    <th style="text-align:left;padding:8px">Item</th>
                    <th style="text-align:center;padding:8px">Qty</th>
                    <th style="text-align:right;padding:8px">Price</th>
                    <th style="text-align:right;padding:8px">Total</th>
                </tr>
            </thead>
            <tbody>
                $rows
                $adjustmentHtml
            </tbody>
        </table>
        """.trimIndent()
    } else ""

    return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <style>
            body { font-family: Arial, sans-serif; padding: 24px; color: #1a1a1a; }
            .header { text-align: center; margin-bottom: 16px; }
            .shop-name { font-size: 20px; font-weight: bold; }
            .invoice-label { color: #ea580c; font-weight: bold; font-size: 16px; }
            .details { display: flex; justify-content: space-between; margin: 12px 0; }
            .total-section { text-align: right; margin-top: 16px; padding-top: 16px; border-top: 2px solid #1a1a1a; }
            .total-amount { font-size: 24px; font-weight: bold; color: #ea580c; }
            .footer { text-align: center; margin-top: 24px; color: #666; font-size: 12px; }
            hr { border: none; border-top: 1px solid #ddd; margin: 12px 0; }
        </style>
    </head>
    <body>
        <div class="header">
            <div class="shop-name">$shopName</div>
            <div class="invoice-label">INVOICE</div>
        </div>
        <hr>
        <div class="details">
            <div>
                <div style="color:#666;font-size:12px">Invoice #</div>
                <div style="font-weight:bold">INV-${transaction.id}</div>
            </div>
            <div style="text-align:right">
                <div style="color:#666;font-size:12px">Date</div>
                <div style="font-weight:bold">$dateStr</div>
            </div>
        </div>
        <div class="details">
            <div>
                <div style="color:#666;font-size:12px">Customer</div>
                <div style="font-weight:bold">${transaction.customerName}</div>
            </div>
            ${if (transaction.customerPhone.isNotBlank()) """
            <div style="text-align:right">
                <div style="color:#666;font-size:12px">Phone</div>
                <div style="font-weight:bold">${transaction.customerPhone}</div>
            </div>
            """ else ""}
        </div>
        <hr>
        $itemsHtml
        <div class="total-section">
            <div style="color:#666;margin-bottom:4px">Total Amount</div>
            <div class="total-amount">${transaction.amount.toCurrency()}</div>
        </div>
        ${if (transaction.dueDate != null) """
        <div style="margin-top:8px;color:#666;font-size:12px;text-align:right">
            Due: ${PkDateTime.formatDisplayDate(transaction.dueDate)}
        </div>
        """ else ""}
        <hr>
        <div class="footer">
            <p>Thank you for your business!</p>
            <p>Generated by Khata One</p>
        </div>
    </body>
    </html>
    """.trimIndent()
}
