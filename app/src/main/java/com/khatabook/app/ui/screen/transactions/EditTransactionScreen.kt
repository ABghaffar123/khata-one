package com.khatabook.app.ui.screen.transactions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khatabook.app.data.entity.EditAction
import com.khatabook.app.ui.screen.customers.CustomerAvatar
import com.khatabook.app.data.entity.TransactionEditLogEntity
import com.khatabook.app.data.entity.TransactionType
import com.khatabook.app.ui.components.SuccessToast
import com.khatabook.app.ui.responsive.*
import com.khatabook.app.ui.theme.ErrorRed
import com.khatabook.app.ui.theme.KhataGradientPresets
import com.khatabook.app.ui.theme.SuccessGreen
import com.khatabook.app.util.PkDateTime
import com.khatabook.app.util.toCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val spacing = LocalResponsiveSpacing.current
    val typography = LocalResponsiveTypography.current
    val gradientTheme = KhataGradientPresets.FireOrange
    var showDatePicker by remember { mutableStateOf(false) }
    var showDueDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.savedSuccessfully, uiState.deletedSuccessfully) {
        if (uiState.savedSuccessfully) {
            SuccessToast.show("Transaction Updated")
        }
        if (uiState.savedSuccessfully || uiState.deletedSuccessfully) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Transaction",
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
                actions = {
                    // History toggle
                    IconButton(onClick = { viewModel.toggleHistory() }) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "Edit History",
                            tint = if (uiState.showHistory) gradientTheme.gradientStart
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Delete
                    IconButton(onClick = { viewModel.showDeleteConfirmation() }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = ErrorRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(spacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(spacing.itemGap)
            ) {
                // ═══ Edit Log History Panel ═══
                AnimatedVisibility(
                    visible = uiState.showHistory,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(spacing.cardPadding)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    tint = gradientTheme.gradientStart,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Edit History",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            if (uiState.editLogs.isEmpty()) {
                                Text(
                                    text = "No changes recorded yet",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            } else {
                                uiState.editLogs.forEach { log ->
                                    EditLogItem(log = log)
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }
                    }
                }

                // ═══ Customer Info ═══
                uiState.customer?.let { customer ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(spacing.cardPadding),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CustomerAvatar(customer = customer, size = 40.dp, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = customer.name,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = customer.phone,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                // ═══ Transaction Type Chips ═══
                Text(
                    text = "Transaction Type",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransactionType.entries.forEach { type ->
                        FilterChip(
                            selected = uiState.type == type,
                            onClick = { viewModel.onTypeChange(type) },
                            label = {
                                Text(
                                    text = type.name.replace("_", " ").lowercase()
                                        .replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            leadingIcon = if (uiState.type == type) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (type) {
                                    TransactionType.PAYMENT_RECEIVED, TransactionType.CASH_SALE ->
                                        SuccessGreen.copy(alpha = 0.12f)
                                    else -> ErrorRed.copy(alpha = 0.12f)
                                },
                                selectedLabelColor = when (type) {
                                    TransactionType.PAYMENT_RECEIVED, TransactionType.CASH_SALE -> SuccessGreen
                                    else -> ErrorRed
                                }
                            )
                        )
                    }
                }

                // ═══ Amount ═══
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = viewModel::onAmountChange,
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = { Text("Rs.", fontWeight = FontWeight.Bold) },
                    isError = uiState.error != null
                )

                // ═══ Description ═══
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                // ═══ Date Picker ═══
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = gradientTheme.gradientStart
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Transaction Date",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = PkDateTime.formatDisplayDate(uiState.date),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                // ═══ Due Date Picker ═══
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDueDatePicker = true },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Event,
                            contentDescription = null,
                            tint = gradientTheme.gradientStart
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Due Date (Optional)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = uiState.dueDate?.let { PkDateTime.formatDisplayDate(it) }
                                    ?: "No due date set",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        if (uiState.dueDate != null) {
                            IconButton(onClick = { viewModel.onDueDateChange(null) }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear date",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                uiState.error?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(spacing.itemGap))

                // ═══ Save Button ═══
                Button(
                    onClick = { viewModel.saveChanges() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving && uiState.amount.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = gradientTheme.gradientStart
                    )
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Changes")
                    }
                }
            }
        }
    }

    // ═══ Delete Confirmation Dialog ═══
    if (uiState.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteConfirmation() },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed) },
            title = { Text("Delete Transaction?") },
            text = {
                Text("This will permanently delete this transaction. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteTransaction() },
                    colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteConfirmation() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ═══ Date Pickers ═══
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.date
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onDateChange(it)
                    }
                    showDatePicker = false
                }) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDueDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.dueDate ?: PkDateTime.todayPktMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDueDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onDueDateChange(it)
                    }
                    showDueDatePicker = false
                }) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDueDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun EditLogItem(log: TransactionEditLogEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Action badge
                Surface(
                    color = when (log.action) {
                        EditAction.EDITED -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        EditAction.DELETED -> ErrorRed.copy(alpha = 0.12f)
                        EditAction.CREATED -> SuccessGreen.copy(alpha = 0.12f)
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = log.action.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (log.action) {
                            EditAction.EDITED -> MaterialTheme.colorScheme.primary
                            EditAction.DELETED -> ErrorRed
                            EditAction.CREATED -> SuccessGreen
                        }
                    )
                }
                Text(
                    text = PkDateTime.formatDisplayDate(log.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            if (log.fieldName != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Field: ${log.fieldName}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            if (log.oldValue != null || log.newValue != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Row {
                    if (log.oldValue != null) {
                        Text(
                            text = "From: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = log.oldValue,
                            style = MaterialTheme.typography.bodySmall,
                            color = ErrorRed.copy(alpha = 0.8f)
                        )
                    }
                }
                if (log.newValue != null) {
                    Row {
                        Text(
                            text = "To: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = log.newValue,
                            style = MaterialTheme.typography.bodySmall,
                            color = SuccessGreen.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            if (log.changedBy.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "By: ${log.changedBy}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}
