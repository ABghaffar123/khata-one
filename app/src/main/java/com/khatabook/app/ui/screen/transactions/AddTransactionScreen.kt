package com.khatabook.app.ui.screen.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khatabook.app.data.entity.TransactionType
import com.khatabook.app.ui.components.SuccessToast
import com.khatabook.app.ui.responsive.*
import com.khatabook.app.ui.theme.ErrorRed
import com.khatabook.app.ui.theme.KhataGradientPresets
import com.khatabook.app.ui.theme.SuccessGreen
import com.khatabook.app.ui.language.LocalStrings
import com.khatabook.app.util.FormValidator
import com.khatabook.app.util.PkDateTime

// ═══ Transaction type display data ═══
private data class TypeCardData(
    val type: TransactionType,
    val label: String,
    val icon: ImageVector,
    val color: Color
)

private val customerTypes = listOf(
    TypeCardData(TransactionType.CREDIT_GIVEN, "Credit Given", Icons.Default.ArrowUpward, Color(0xFFFF9800)),
    TypeCardData(TransactionType.PAYMENT_RECEIVED, "Payment Received", Icons.Default.ArrowDownward, Color(0xFF4CAF50)),
    TypeCardData(TransactionType.CASH_SALE, "Cash Sale", Icons.Default.Payments, Color(0xFF2196F3)),
    TypeCardData(TransactionType.SALE_RETURN, "Sale Return", Icons.Default.Replay, Color(0xFFEF5350)),
)

private val businessTypes = listOf(
    TypeCardData(TransactionType.STOCK_PURCHASED, "Stock Purchased", Icons.Default.ShoppingCart, Color(0xFF9C27B0)),
    TypeCardData(TransactionType.PURCHASE_RETURN, "Purchase Return", Icons.Default.SwapHoriz, Color(0xFF009688)),
    TypeCardData(TransactionType.EXPENSE, "Expense", Icons.Default.ReceiptLong, Color(0xFFF44336)),
    TypeCardData(TransactionType.OWNER_WITHDRAWAL, "Owner Withdrawal", Icons.Default.AccountBalanceWallet, Color(0xFFFF5722)),
    TypeCardData(TransactionType.OWNER_INVESTMENT, "Owner Investment", Icons.Default.Savings, Color(0xFF388E3C)),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onNavigateBack: () -> Unit,
    onNavigateToInvoice: (Long) -> Unit = {},
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val spacing = LocalResponsiveSpacing.current
    val typography = LocalResponsiveTypography.current
    val gradientTheme = KhataGradientPresets.FireOrange
    val strings = LocalStrings.current
    var showCustomerDropdown by remember { mutableStateOf(false) }
    var showSupplierDropdown by remember { mutableStateOf(false) }
    var showAccountDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    var amountError by remember { mutableStateOf<String?>(null) }
    val isFormValid = uiState.amount.isNotBlank() && amountError == null &&
            (uiState.showNoPartyField || uiState.selectedCustomer != null || uiState.selectedSupplier != null)

    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) {
            SuccessToast.show(strings.transactionSaved)
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.newTransaction,
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = typography.h2),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(spacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(spacing.itemGap)
        ) {
            // ═══ 1. Transaction Type Selector ═══
            Text(
                text = "Transaction Type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Customer Transactions Section
            Text(
                text = "Customer Transactions",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                customerTypes.forEach { data ->
                    TypeCard(
                        data = data,
                        isSelected = uiState.type == data.type,
                        onClick = { viewModel.onTypeChange(data.type) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Business Transactions Section
            Text(
                text = "Business Transactions",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                businessTypes.take(3).forEach { data ->
                    TypeCard(
                        data = data,
                        isSelected = uiState.type == data.type,
                        onClick = { viewModel.onTypeChange(data.type) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                businessTypes.drop(3).forEach { data ->
                    TypeCard(
                        data = data,
                        isSelected = uiState.type == data.type,
                        onClick = { viewModel.onTypeChange(data.type) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Spacer for alignment when last row has fewer items
                if (businessTypes.drop(3).size < 3) {
                    repeat(3 - businessTypes.drop(3).size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // ═══ 2. Customer/Supplier Selector (dynamic) ═══
            if (uiState.showCustomerField) {
                Text(
                    text = if (uiState.type.customerRequired) "Select a Customer *" else "Select a Customer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                ExposedDropdownMenuBox(
                    expanded = showCustomerDropdown,
                    onExpandedChange = { showCustomerDropdown = it }
                ) {
                    OutlinedTextField(
                        value = uiState.selectedCustomer?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Customer") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCustomerDropdown) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp)) }
                    )
                    ExposedDropdownMenu(
                        expanded = showCustomerDropdown,
                        onDismissRequest = { showCustomerDropdown = false }
                    ) {
                        uiState.customers.forEach { customer ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(customer.name, fontWeight = FontWeight.Medium)
                                        Text(customer.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    }
                                },
                                onClick = {
                                    viewModel.onCustomerSelected(customer)
                                    showCustomerDropdown = false
                                }
                            )
                        }
                        if (uiState.customers.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No customers found", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) },
                                onClick = { showCustomerDropdown = false }
                            )
                        }
                    }
                }
            }

            if (uiState.showSupplierField) {
                Text(
                    text = "Select a Supplier (Optional)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                ExposedDropdownMenuBox(
                    expanded = showSupplierDropdown,
                    onExpandedChange = { showSupplierDropdown = it }
                ) {
                    OutlinedTextField(
                        value = uiState.selectedSupplier?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Supplier") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSupplierDropdown) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        leadingIcon = { Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(20.dp)) }
                    )
                    ExposedDropdownMenu(
                        expanded = showSupplierDropdown,
                        onDismissRequest = { showSupplierDropdown = false }
                    ) {
                        uiState.suppliers.forEach { supplier ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(supplier.name, fontWeight = FontWeight.Medium)
                                        Text(supplier.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    }
                                },
                                onClick = {
                                    viewModel.onSupplierSelected(supplier)
                                    showSupplierDropdown = false
                                }
                            )
                        }
                        if (uiState.suppliers.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No suppliers found", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) },
                                onClick = { showSupplierDropdown = false }
                            )
                        }
                    }
                }
            }

            if (uiState.showNoPartyField) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "No customer or supplier needed for this transaction type",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // ═══ 3. Amount ═══
            OutlinedTextField(
                value = uiState.amount,
                onValueChange = {
                    viewModel.onAmountChange(it)
                    amountError = FormValidator.requiredAmount(it)
                },
                label = { Text("Amount *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Text("Rs.", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                isError = amountError != null,
                supportingText = {
                    val amt = uiState.amount.toDoubleOrNull()
                    if (amt != null && amt > 0) {
                        Text(
                            text = "Rs ${String.format("%,.0f", amt)}",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        amountError?.let { err ->
                            Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            )

            // ═══ 4. Description ═══
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            // ═══ 5. Due Date (only for Credit Given) ═══
            if (uiState.showDueDateField) {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = gradientTheme.gradientStart)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Due Date *",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = uiState.dueDate?.let { PkDateTime.formatDisplayDate(it) }
                                    ?: "Select due date for payment reminder",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        if (uiState.dueDate != null) {
                            IconButton(onClick = { viewModel.onDueDateSelected(null) }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            // ═══ 6. Account Selector ═══
            Text(
                text = "Cash Account",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            ExposedDropdownMenuBox(
                expanded = showAccountDropdown,
                onExpandedChange = { showAccountDropdown = it }
            ) {
                OutlinedTextField(
                    value = uiState.selectedAccount?.name ?: "Select Account",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Account") },
                    leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = gradientTheme.gradientStart) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showAccountDropdown) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showAccountDropdown,
                    onDismissRequest = { showAccountDropdown = false }
                ) {
                    uiState.accounts.forEach { account ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(account.name, fontWeight = FontWeight.Medium)
                                    Text(account.type.displayName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                            },
                            onClick = {
                                viewModel.onAccountSelected(account)
                                showAccountDropdown = false
                            }
                        )
                    }
                }
            }

            // ═══ 7. Live Preview Card ═══
            if (uiState.amount.isNotBlank() && uiState.amount.toDoubleOrNull() != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = customerTypes.find { it.type == uiState.type }?.color?.copy(alpha = 0.08f)
                            ?: businessTypes.find { it.type == uiState.type }?.color?.copy(alpha = 0.08f)
                            ?: MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val typeData = (customerTypes + businessTypes).find { it.type == uiState.type }
                        typeData?.let {
                            Box(
                                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(it.color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(it.icon, contentDescription = null, tint = it.color, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Text(
                            text = uiState.previewText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // ═══ Error ═══
            uiState.error?.let { error ->
                Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(spacing.itemGap))

            // ═══ 8. Save Button ═══
            Button(
                onClick = { viewModel.saveTransaction() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving && isFormValid,
                colors = ButtonDefaults.buttonColors(containerColor = gradientTheme.gradientStart)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(uiState.saveButtonText)
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dueDate ?: PkDateTime.todayPktMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.onDueDateSelected(it) }
                    showDatePicker = false
                }) { Text(strings.continueText) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(strings.cancel) }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
private fun TypeCard(
    data: TypeCardData,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(72.dp)
            .then(
                if (isSelected) Modifier.border(2.dp, data.color, RoundedCornerShape(12.dp))
                else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) data.color.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(data.color.copy(alpha = if (isSelected) 0.25f else 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(data.icon, contentDescription = null, tint = data.color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = data.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) data.color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                fontSize = 9.sp
            )
        }
    }
}
