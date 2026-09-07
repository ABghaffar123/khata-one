package com.khatabook.app.ui.screen.reports

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import com.khatabook.app.data.entity.TransactionType
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khatabook.app.ui.language.LocalStrings
import com.khatabook.app.ui.responsive.*
import com.khatabook.app.ui.theme.ErrorRed
import com.khatabook.app.ui.theme.KhataGradientPresets
import com.khatabook.app.ui.theme.SuccessGreen
import com.khatabook.app.ui.theme.WarningOrange
import com.khatabook.app.util.PkDateTime
import com.khatabook.app.util.ReportExporter
import com.khatabook.app.util.toCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigateToDailySummary: () -> Unit = {},
    onNavigateBack: (() -> Unit)? = null,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val cashBook by viewModel.cashBook.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val strings = LocalStrings.current
    val spacing = LocalResponsiveSpacing.current
    val typography = LocalResponsiveTypography.current
    val gradientTheme = KhataGradientPresets.FireOrange

    var showOpeningCashDialog by remember { mutableStateOf(false) }
    var showCustomDatePicker by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }
    var showAccountDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.cashBookAndReports,
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = typography.h2),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    onNavigateBack?.let {
                        IconButton(onClick = it) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    // Export menu
                    Box {
                        IconButton(onClick = { showExportMenu = true }) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Export",
                                tint = gradientTheme.gradientStart
                            )
                        }
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = ErrorRed
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(strings.exportPdf)
                                    }
                                },
                                onClick = {
                                    showExportMenu = false
                                    viewModel.exportPdf()?.let { (_, intent) ->
                                        context.startActivity(Intent.createChooser(intent, "Export Report"))
                                    } ?: Toast.makeText(context, "No data to export", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.TableChart,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = SuccessGreen
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(strings.exportExcelCsv)
                                    }
                                },
                                onClick = {
                                    showExportMenu = false
                                    viewModel.exportCsv()?.let { (_, intent) ->
                                        context.startActivity(Intent.createChooser(intent, "Export Report"))
                                    } ?: Toast.makeText(context, "No data to export", Toast.LENGTH_SHORT).show()
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
            // ═══ Date Range Selector ═══
            item {
                DateRangeChips(
                    selectedRange = cashBook.dateRange,
                    onSelectRange = { viewModel.setDateRange(it) },
                    onSelectCustom = { showCustomDatePicker = true }
                )
            }

            // ═══ Account Filter ═══
            if (accounts.isNotEmpty()) {
                item {
                    ExposedDropdownMenuBox(
                        expanded = showAccountDropdown,
                        onExpandedChange = { showAccountDropdown = it }
                    ) {
                        val selectedAccountName = cashBook.selectedAccountId?.let { id ->
                            accounts.find { it.id == id }?.name
                        } ?: "All Accounts"

                        OutlinedTextField(
                            value = selectedAccountName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Filter by Account") },
                            leadingIcon = {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null)
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showAccountDropdown) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = showAccountDropdown,
                            onDismissRequest = { showAccountDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text("All Accounts", fontWeight = if (cashBook.selectedAccountId == null) FontWeight.Bold else FontWeight.Normal)
                                },
                                onClick = {
                                    viewModel.setSelectedAccount(null)
                                    showAccountDropdown = false
                                }
                            )
                            accounts.forEach { account ->
                                DropdownMenuItem(
                                    text = {
                                        Text(account.name, fontWeight = if (cashBook.selectedAccountId == account.id) FontWeight.Bold else FontWeight.Normal)
                                    },
                                    onClick = {
                                        viewModel.setSelectedAccount(account.id)
                                        showAccountDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // ═══ Opening Cash Card ═══
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(gradientTheme.gradientStart.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = gradientTheme.gradientStart, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(strings.openingCash, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Text(cashBook.openingCash.toCurrency(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { showOpeningCashDialog = true }) {
                            Text(strings.edit, color = gradientTheme.gradientStart)
                        }
                    }
                }
            }

            // ═══ Cash In Hand (Galla Balance) ═══
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(strings.cashInHand, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            cashBook.closingCash.toCurrency(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (cashBook.closingCash >= 0) SuccessGreen else ErrorRed
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        // Cash In vs Cash Out
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(strings.cashIn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                Text(
                                    (cashBook.totalCashSales + cashBook.totalPaymentsReceived).toCurrency(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(strings.cashOut, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                Text(
                                    (cashBook.totalExpenses + cashBook.totalPaymentsToSuppliers).toCurrency(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ErrorRed
                                )
                            }
                        }
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
                    Box(modifier = Modifier.weight(1f)) { ReportCard(strings.cashSales, cashBook.totalCashSales.toCurrency(), Icons.Default.PointOfSale, SuccessGreen) }
                    Box(modifier = Modifier.weight(1f)) { ReportCard(strings.paymentsReceived, cashBook.totalPaymentsReceived.toCurrency(), Icons.Default.ArrowDownward, SuccessGreen) }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.itemGap)) {
                    Box(modifier = Modifier.weight(1f)) { ReportCard(strings.creditGiven, cashBook.totalCreditGiven.toCurrency(), Icons.Default.ArrowUpward, ErrorRed) }
                    Box(modifier = Modifier.weight(1f)) { ReportCard(strings.expenses, cashBook.totalExpenses.toCurrency(), Icons.Default.Receipt, ErrorRed) }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.itemGap)) {
                    Box(modifier = Modifier.weight(1f)) { ReportCard(strings.purchases, cashBook.totalPurchases.toCurrency(), Icons.Default.ShoppingCart, WarningOrange) }
                    Box(modifier = Modifier.weight(1f)) { ReportCard(strings.paymentsToSuppliers, cashBook.totalPaymentsToSuppliers.toCurrency(), Icons.Default.CheckCircle, WarningOrange) }
                }
            }

            // ═══ Profit Estimator ═══
            item {
                Text(
                    strings.businessOverview,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = spacing.itemGap)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(gradientTheme.gradientStart.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = gradientTheme.gradientStart, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(strings.estimatedProfit, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        ProfitRow(strings.stockValue, cashBook.stockValue.toCurrency())
                        ProfitRow(strings.potentialRevenue, cashBook.potentialRevenue.toCurrency())
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        ProfitRow(
                            strings.estimatedProfit,
                            cashBook.estimatedProfit.toCurrency(),
                            if (cashBook.estimatedProfit >= 0) SuccessGreen else ErrorRed
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            if (cashBook.dateRange == DateRange.TODAY) "Based on current inventory purchase vs. sale prices"
                            else "Period: ${cashBook.totalTransactionCount} transactions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            // ═══ Today's Stats ═══
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${cashBook.totalTransactionCount}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text(strings.todayTransactions, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            // ═══ Download Summary Button ═══
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onNavigateToDailySummary,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = gradientTheme.gradientStart)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.dailySummary)
                    }
                }
            }

            // ═══ Search Bar ═══
            item {
                var searchText by remember { mutableStateOf(cashBook.searchQuery) }
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it; viewModel.setSearchQuery(it) },
                    placeholder = { Text(strings.searchTransactions) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = { searchText = ""; viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ═══ Transaction Type Filter Chips ═══
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TypeFilterChip(strings.allTypes, cashBook.selectedTransactionType == null) { viewModel.setSelectedTransactionType(null) }
                    TypeFilterChip(strings.cashSale, cashBook.selectedTransactionType == TransactionType.CASH_SALE) { viewModel.setSelectedTransactionType(TransactionType.CASH_SALE) }
                    TypeFilterChip(strings.paymentReceived, cashBook.selectedTransactionType == TransactionType.PAYMENT_RECEIVED) { viewModel.setSelectedTransactionType(TransactionType.PAYMENT_RECEIVED) }
                    TypeFilterChip(strings.creditGiven, cashBook.selectedTransactionType == TransactionType.CREDIT_GIVEN { viewModel.setSelectedTransactionType(TransactionType.CREDIT_GIVEN }
                    TypeFilterChip(strings.expenses, cashBook.selectedTransactionType == TransactionType.EXPENSE) { viewModel.setSelectedTransactionType(TransactionType.EXPENSE) }
                }
            }

            // ═══ Filtered Transaction List ═══
            if (filteredTransactions.isNotEmpty()) {
                item {
                    Text(
                        "${strings.transactions} (${filteredTransactions.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = spacing.itemGap)
                    )
                }
            }

            items(filteredTransactions.size) { index ->
                val txn = filteredTransactions[index]
                val isPositive = txn.type == TransactionType.PAYMENT_RECEIVED || txn.type == TransactionType.CASH_SALE
                val color = if (isPositive) SuccessGreen else ErrorRed
                val typeLabel = when (txn.type) {
                    TransactionType.CASH_SALE -> strings.cashSale
                    TransactionType.PAYMENT_RECEIVED -> strings.paymentReceived
                    TransactionType.CREDIT_GIVEN -> strings.creditGiven
                    TransactionType.EXPENSE -> strings.expenses
                    TransactionType.STOCK_PURCHASED -> strings.purchases
                    TransactionType.SALE_RETURN -> "Sale Return"
                    TransactionType.PURCHASE_RETURN -> "Purchase Return"
                    TransactionType.OWNER_WITHDRAWAL -> "Owner Withdrawal"
                    TransactionType.OWNER_INVESTMENT -> "Owner Investment"
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(color.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isPositive) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(txn.customerName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text(typeLabel, style = MaterialTheme.typography.bodySmall, color = color)
                            if (!txn.description.isNullOrBlank()) {
                                Text(txn.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), maxLines = 1)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "${if (isPositive) "+" else "-"}${txn.amount.toCurrency()}",
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                            Text(
                                PkDateTime.formatDate(txn.date),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }

            if (filteredTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (cashBook.searchQuery.isNotEmpty() || cashBook.selectedTransactionType != null) strings.noTransactionsFound else "No transactions yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            if (cashBook.searchQuery.isEmpty() && cashBook.selectedTransactionType == null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Add a sale or expense entry to see your reports here",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(spacing.screenVertical)) }
        }
    }

    // ═══ Opening Cash Dialog ═══
    if (showOpeningCashDialog) {
        var cashInput by remember { mutableStateOf(cashBook.openingCash.toLong().toString()) }
        AlertDialog(
            onDismissRequest = { showOpeningCashDialog = false },
            title = { Text(strings.openingCash, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = cashInput,
                    onValueChange = { cashInput = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text(strings.amount) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateOpeningCash(cashInput.toDoubleOrNull() ?: 0.0)
                    showOpeningCashDialog = false
                }) { Text(strings.save) }
            },
            dismissButton = {
                TextButton(onClick = { showOpeningCashDialog = false }) { Text(strings.cancel) }
            }
        )
    }

    // ═══ Custom Date Range Picker ═══
    if (showCustomDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showCustomDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val start = PkDateTime.startOfDayPkt(it)
                        val end = PkDateTime.endOfDayPkt(it)
                        viewModel.setCustomDateRange(start, end)
                    }
                    showCustomDatePicker = false
                }) { Text(strings.continueText) }
            },
            dismissButton = { TextButton(onClick = { showCustomDatePicker = false }) { Text(strings.cancel) } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun DateRangeChips(
    selectedRange: DateRange,
    onSelectRange: (DateRange) -> Unit,
    onSelectCustom: () -> Unit
) {
    val strings = LocalStrings.current
    val spacing = LocalResponsiveSpacing.current
    val gradientTheme = KhataGradientPresets.FireOrange

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DateChip(strings.today, selectedRange == DateRange.TODAY) { onSelectRange(DateRange.TODAY) }
        DateChip(strings.thisWeek, selectedRange == DateRange.THIS_WEEK) { onSelectRange(DateRange.THIS_WEEK) }
        DateChip(strings.thisMonthShort, selectedRange == DateRange.THIS_MONTH) { onSelectRange(DateRange.THIS_MONTH) }
        DateChip(strings.custom, selectedRange == DateRange.CUSTOM) { onSelectCustom() }
    }
}

@Composable
private fun DateChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val gradientTheme = KhataGradientPresets.FireOrange
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text, style = MaterialTheme.typography.bodySmall) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = gradientTheme.gradientStart.copy(alpha = 0.12f),
            selectedLabelColor = gradientTheme.gradientStart
        )
    )
}

@Composable
private fun ReportCard(title: String, amount: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
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
                Text(amount, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
            }
        }
    }
}

@Composable
private fun ProfitRow(label: String, amount: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(amount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun TypeFilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val gradientTheme = KhataGradientPresets.FireOrange
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text, style = MaterialTheme.typography.bodySmall) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = gradientTheme.gradientStart.copy(alpha = 0.12f),
            selectedLabelColor = gradientTheme.gradientStart
        )
    )
}
