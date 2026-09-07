package com.khatabook.app.ui.screen.reports

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khatabook.app.ui.components.SuccessToast
import com.khatabook.app.ui.language.LocalStrings
import com.khatabook.app.ui.responsive.*
import com.khatabook.app.ui.theme.ErrorRed
import com.khatabook.app.ui.theme.KhataGradientPresets
import com.khatabook.app.ui.theme.SuccessGreen
import com.khatabook.app.ui.theme.WarningOrange
import com.khatabook.app.util.toCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailySummaryScreen(
    onNavigateBack: () -> Unit,
    viewModel: DailySummaryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val strings = LocalStrings.current
    val spacing = LocalResponsiveSpacing.current
    val typography = LocalResponsiveTypography.current
    val gradientTheme = KhataGradientPresets.FireOrange

    var showDownloadMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.dailySummary,
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = typography.h2),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Download menu
                    Box {
                        IconButton(onClick = { showDownloadMenu = true }) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = "Download",
                                tint = gradientTheme.gradientStart
                            )
                        }
                        DropdownMenu(
                            expanded = showDownloadMenu,
                            onDismissRequest = { showDownloadMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(20.dp), tint = ErrorRed)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(strings.downloadPdf)
                                    }
                                },
                                onClick = {
                                    showDownloadMenu = false
                                    if (viewModel.downloadPdf() != null) {
                                        SuccessToast.show("PDF saved to Downloads")
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(20.dp), tint = SuccessGreen)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(strings.downloadImage)
                                    }
                                },
                                onClick = {
                                    showDownloadMenu = false
                                    if (viewModel.downloadImage() != null) {
                                        SuccessToast.show("Image saved to Downloads")
                                    }
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(spacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(spacing.itemGap)
        ) {
            // ═══ Date Header ═══
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = gradientTheme.gradientStart.copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = gradientTheme.gradientStart)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(state.date, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        if (state.isDownloaded) {
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.CheckCircle, contentDescription = "Downloaded", tint = SuccessGreen, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // ═══ Opening Cash ═══
            item {
                SummaryCard(strings.openingCash, state.openingCash, Icons.Default.AccountBalanceWallet, gradientTheme.gradientStart)
            }

            // ═══ Cash In / Cash Out ═══
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.itemGap)) {
                    Box(modifier = Modifier.weight(1f)) { SummaryCard(strings.cashIn, state.cashIn, Icons.Default.ArrowDownward, SuccessGreen) }
                    Box(modifier = Modifier.weight(1f)) { SummaryCard(strings.cashOut, state.cashOut, Icons.Default.ArrowUpward, ErrorRed) }
                }
            }

            // ═══ Cash in Hand ═══
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(strings.cashInHand, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            state.cashInHand.toCurrency(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (state.cashInHand >= 0) SuccessGreen else ErrorRed
                        )
                    }
                }
            }

            // ═══ Income Breakdown ═══
            item {
                Text(
                    strings.incomeBreakdown,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = spacing.itemGap)
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.itemGap)) {
                    Box(modifier = Modifier.weight(1f)) { SummaryCard(strings.cashSales, state.cashSales, Icons.Default.PointOfSale, SuccessGreen) }
                    Box(modifier = Modifier.weight(1f)) { SummaryCard(strings.paymentsReceived, state.paymentsReceived, Icons.Default.ArrowDownward, SuccessGreen) }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.itemGap)) {
                    Box(modifier = Modifier.weight(1f)) { SummaryCard(strings.creditGiven, state.creditGiven, Icons.Default.ArrowUpward, ErrorRed) }
                    Box(modifier = Modifier.weight(1f)) { SummaryCard(strings.expenses, state.expenses, Icons.Default.Receipt, ErrorRed) }
                }
            }

            // ═══ Download History ═══
            if (state.downloadedHistory.isNotEmpty()) {
                item {
                    Text(
                        strings.downloadHistory,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = spacing.itemGap)
                    )
                }

                items(state.downloadedHistory.size) { index ->
                    val record = state.downloadedHistory[index]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (record.fileType == "pdf") Icons.Default.PictureAsPdf else Icons.Default.Image,
                                contentDescription = null,
                                tint = if (record.fileType == "pdf") ErrorRed else SuccessGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(record.displayDate, fontWeight = FontWeight.SemiBold)
                                Text(record.fileType.uppercase(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                            IconButton(onClick = {
                                try {
                                    val file = java.io.File(record.filePath)
                                    if (file.exists()) {
                                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                        val shareIntent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, if (record.fileType == "pdf") "application/pdf" else "image/png")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Open Summary"))
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(Icons.Default.OpenInNew, contentDescription = "Open")
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(spacing.screenVertical)) }
        }
    }
}

@Composable
private fun SummaryCard(title: String, amount: Double, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text(amount.toCurrency(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
            }
        }
    }
}
