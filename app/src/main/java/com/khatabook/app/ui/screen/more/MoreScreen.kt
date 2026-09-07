package com.khatabook.app.ui.screen.more

import android.content.ClipData
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.khatabook.app.data.KhataDatabase
import com.khatabook.app.ui.components.SuccessToast
import com.khatabook.app.ui.responsive.LocalResponsiveSpacing
import com.khatabook.app.ui.responsive.LocalResponsiveTypography
import com.khatabook.app.ui.responsive.LocalWindowSize
import com.khatabook.app.ui.responsive.MaxWidthContainer
import com.khatabook.app.ui.responsive.ResponsiveCard
import com.khatabook.app.ui.responsive.ResponsiveSettingsItem
import com.khatabook.app.ui.responsive.WindowWidthSizeClass
import com.khatabook.app.ui.theme.ErrorRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * ═══════════════════════════════════════════════════════════════
 * MORE SCREEN — replaces the old Settings + Reports bottom tabs.
 *
 * Organised into clear sections:
 *   Business  → Reports, Cash Accounts, Daily Summary
 *   Settings  → Language, Theme, Security, Backup Data, Clear All Data
 *   About     → About Khata One, Rate App
 * ═══════════════════════════════════════════════════════════════
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    currentLanguageLabel: String = "",
    onOpenReports: () -> Unit = {},
    onOpenCashAccounts: () -> Unit = {},
    onOpenDailySummary: () -> Unit = {},
    onOpenLanguage: () -> Unit = {},
    onOpenTheme: () -> Unit = {},
    onOpenSecurity: () -> Unit = {}
) {
    val context = LocalContext.current
    val windowSize = LocalWindowSize.current
    val spacing = LocalResponsiveSpacing.current
    val typography = LocalResponsiveTypography.current
    val scope = rememberCoroutineScope()

    // ═══ Local state for in-screen actions ═══
    var showBackupPreview by remember { mutableStateOf(false) }
    var backupJson by remember { mutableStateOf<String?>(null) }
    var isBackingUp by remember { mutableStateOf(false) }
    var isClearing by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val db = remember { KhataDatabase.getDatabase(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "More",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = typography.h2),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        val businessItems: @Composable () -> Unit = {
            MoreGroupCard {
                ResponsiveSettingsItem(
                    icon = Icons.Default.BarChart,
                    title = "Reports",
                    subtitle = "Cash book, summary & daily report",
                    onClick = onOpenReports
                )
                ResponsiveSettingsItem(
                    icon = Icons.Default.AccountBalance,
                    title = "Cash Accounts",
                    subtitle = "Manage accounts",
                    onClick = onOpenCashAccounts
                )
                ResponsiveSettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Daily Summary",
                    subtitle = "Daily business summary at 9 PM",
                    onClick = onOpenDailySummary
                )
            }
        }
        val settingsItems: @Composable () -> Unit = {
            MoreGroupCard {
                ResponsiveSettingsItem(
                    icon = Icons.Default.Language,
                    title = "Language",
                    subtitle = currentLanguageLabel.ifBlank { "English" },
                    onClick = onOpenLanguage
                )
                ResponsiveSettingsItem(
                    icon = Icons.Default.Brightness6,
                    title = "Theme",
                    subtitle = "Appearance & gradient",
                    onClick = onOpenTheme
                )
                ResponsiveSettingsItem(
                    icon = Icons.Default.Security,
                    title = "Security Settings",
                    subtitle = "PIN, biometric, auto-lock",
                    onClick = onOpenSecurity
                )
                ResponsiveSettingsItem(
                    icon = Icons.Default.Storage,
                    title = "Backup Data",
                    subtitle = "Export as JSON",
                    onClick = {
                        if (!isBackingUp) {
                            isBackingUp = true
                            scope.launch {
                                runCatching {
                                    withContext(Dispatchers.IO) { buildBackupJson(db) }
                                }.onSuccess { json ->
                                    backupJson = json
                                    showBackupPreview = true
                                }.onFailure { e ->
                                    Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                                isBackingUp = false
                            }
                        }
                    }
                )
                ResponsiveSettingsItem(
                    icon = Icons.Default.DeleteForever,
                    title = "Clear All Data",
                    subtitle = "Remove all customers & transactions",
                    onClick = { showClearConfirm = true }
                )
            }
        }
        val aboutItems: @Composable () -> Unit = {
            MoreGroupCard {
                ResponsiveSettingsItem(
                    icon = Icons.Default.Info,
                    title = "About Khata One",
                    subtitle = "Version 1.0.0",
                    onClick = { showAboutDialog = true }
                )
                ResponsiveSettingsItem(
                    icon = Icons.Default.Star,
                    title = "Rate App",
                    subtitle = "Rate on Play Store",
                    onClick = {
                        runCatching {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    android.net.Uri.parse("market://details?id=${context.packageName}")
                                )
                            )
                        }.onFailure {
                            Toast.makeText(context, "Play Store not available", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }

        if (windowSize.widthSizeClass >= WindowWidthSizeClass.Expanded) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.spacedBy(spacing.sectionGap)
            ) {
                item {
                    MaxWidthContainer {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing.sectionGap)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                MoreSection(title = "Business") { businessItems() }
                                Spacer(modifier = Modifier.height(spacing.sectionGap))
                                MoreSection(title = "About") { aboutItems() }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                MoreSection(title = "Settings") { settingsItems() }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.spacedBy(spacing.sectionGap / 2)
            ) {
                item {
                    MaxWidthContainer { MoreSection(title = "Business") { businessItems() } }
                }
                item {
                    MaxWidthContainer { MoreSection(title = "Settings") { settingsItems() } }
                }
                item {
                    MaxWidthContainer { MoreSection(title = "About") { aboutItems() } }
                }
                item { Spacer(modifier = Modifier.height(spacing.screenVertical)) }
            }
        }
    }

    // ═══ Backup preview dialog ═══
    if (showBackupPreview && backupJson != null) {
        AlertDialog(
            onDismissRequest = { showBackupPreview = false },
            title = { Text("Backup Data", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = backupJson ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showBackupPreview = false
                    scope.launch {
                        val shareJson = backupJson ?: ""
                        val shareIntent = withContext(Dispatchers.IO) {
                            runCatching { createBackupShareIntent(context, shareJson) }.getOrNull()
                        }
                        if (shareIntent != null) {
                            runCatching {
                                context.startActivity(Intent.createChooser(shareIntent, "Share Backup"))
                            }.onFailure {
                                Toast.makeText(context, "Backup failed: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Backup failed: could not create file", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) { Text("Save / Share") }
            },
            dismissButton = {
                TextButton(onClick = { showBackupPreview = false }) { Text("Close") }
            }
        )
    }

    // ═══ Clear all data confirmation ═══
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Data", fontWeight = FontWeight.Bold, color = ErrorRed) },
            text = {
                Text(
                    "This will permanently delete all customers, suppliers, products, " +
                        "transactions and accounts. This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showClearConfirm = false
                    if (!isClearing) {
                        isClearing = true
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                runCatching { db.clearAllTables() }
                            }
                            isClearing = false
                            SuccessToast.show("All data cleared")
                        }
                    }
                }) { Text("Delete All", color = ErrorRed) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") }
            }
        )
    }

    // ═══ About dialog ═══
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About Khata One", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Khata One — the simple khata book for your shop.\n\n" +
                        "Version 1.0.0"
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun MoreSection(
    title: String,
    content: @Composable () -> Unit
) {
    val typography = LocalResponsiveTypography.current
    val spacing = LocalResponsiveSpacing.current

    Column(modifier = Modifier.padding(vertical = spacing.itemGap)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = typography.labelLg),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = spacing.itemGap)
        )
        content()
    }
}

@Composable
private fun MoreGroupCard(content: @Composable () -> Unit) {
    ResponsiveCard { content() }
}

// ═══════════════════════════════════════════════════════════════
// BACKUP (JSON export) helpers
// ═══════════════════════════════════════════════════════════════

private suspend fun buildBackupJson(db: KhataDatabase): String {
    val root = JSONObject()
    root.put("app", "Khata One")
    root.put("version", "1.0.0")
    root.put("exportedAt", System.currentTimeMillis())

    root.put("customers", JSONArray().apply {
        db.customerDao().getAllCustomers().first().forEach { c ->
            put(JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("phone", c.phone)
                put("whatsappNumber", c.whatsappNumber ?: "")
                put("address", c.address ?: "")
                put("notes", c.notes ?: "")
                put("totalCredit", c.totalCredit)
                put("totalPayment", c.totalPayment)
            })
        }
    })

    root.put("suppliers", JSONArray().apply {
        db.supplierDao().getAllSuppliers().first().forEach { s ->
            put(JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("phone", s.phone)
                put("shopName", s.shopName ?: "")
                put("address", s.address ?: "")
                put("notes", s.notes ?: "")
                put("totalCreditTaken", s.totalCreditTaken)
                put("totalPaymentGiven", s.totalPaymentGiven)
            })
        }
    })

    root.put("products", JSONArray().apply {
        db.productDao().getAllProducts().first().forEach { p ->
            put(JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("barcode", p.barcode ?: "")
                put("unit", p.unit.name)
                put("salePrice", p.salePrice)
                put("purchasePrice", p.purchasePrice)
                put("stockQuantity", p.stockQuantity)
                put("lowStockThreshold", p.lowStockThreshold)
                put("isActive", p.isActive)
            })
        }
    })

    root.put("transactions", JSONArray().apply {
        db.transactionDao().getAllTransactions().first().forEach { t ->
            put(JSONObject().apply {
                put("id", t.id)
                put("customerId", t.customerId)
                put("customerName", t.customerName)
                put("type", t.type.name)
                put("amount", t.amount)
                put("description", t.description ?: "")
                put("dueDate", t.dueDate ?: 0)
                put("accountId", t.accountId ?: 0)
                put("supplierId", t.supplierId ?: 0)
                put("supplierName", t.supplierName ?: "")
                put("date", t.date)
            })
        }
    })

    root.put("cashAccounts", JSONArray().apply {
        db.cashAccountDao().getAllActiveAccounts().first().forEach { a ->
            put(JSONObject().apply {
                put("id", a.id)
                put("name", a.name)
                put("type", a.type.name)
                put("balance", a.balance)
                put("isDefault", a.isDefault)
                put("isActive", a.isActive)
            })
        }
    })

    return root.toString(2)
}

private fun createBackupShareIntent(context: android.content.Context, json: String): Intent {
    val fileName = "KhataOne_Backup_${System.currentTimeMillis()}.json"
    val file = File(context.cacheDir, fileName)
    file.writeText(json)

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    return Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Khata One Backup")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        clipData = ClipData.newRawUri("Khata One Backup", uri)
    }
}
