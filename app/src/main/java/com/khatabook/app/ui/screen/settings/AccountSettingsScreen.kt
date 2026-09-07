package com.khatabook.app.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khatabook.app.data.entity.CashAccountEntity
import com.khatabook.app.data.entity.CashAccountType
import com.khatabook.app.ui.components.SuccessToast
import com.khatabook.app.ui.language.LocalStrings
import com.khatabook.app.ui.responsive.LocalResponsiveSpacing
import com.khatabook.app.ui.responsive.LocalResponsiveTypography
import com.khatabook.app.ui.theme.ErrorRed
import com.khatabook.app.ui.theme.KhataGradientPresets
import com.khatabook.app.ui.theme.SuccessGreen
import com.khatabook.app.util.toCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AccountSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val spacing = LocalResponsiveSpacing.current
    val typography = LocalResponsiveTypography.current
    val strings = LocalStrings.current
    val gradientTheme = KhataGradientPresets.FireOrange

    // Show a global toast when an account is added/updated
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            SuccessToast.show("Account Saved")
            viewModel.consumeSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cash Accounts",
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
                    IconButton(onClick = { viewModel.startAddAccount() }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Account",
                            tint = gradientTheme.gradientStart
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (uiState.accounts.isEmpty() && !uiState.isAdding) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AccountBalance,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No accounts yet",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap + to add your first cash account",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(spacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(spacing.itemGap)
            ) {
                // Info card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = gradientTheme.gradientStart,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Manage your cash accounts. Each transaction can be linked to a specific account.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Account list
                items(uiState.accounts, key = { it.id }) { account ->
                    AccountItem(
                        account = account,
                        onEdit = { viewModel.startEditAccount(account) },
                        onDelete = { viewModel.deleteAccount(account) },
                        onSetDefault = { viewModel.setDefault(account) }
                    )
                }

                // Bottom spacing
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // Add/Edit Account Dialog
    if (uiState.isAdding) {
        AddEditAccountDialog(
            name = uiState.newName,
            type = uiState.newType,
            isEditing = uiState.editingAccount != null,
            error = uiState.error,
            onNameChange = viewModel::onNameChange,
            onTypeChange = viewModel::onTypeChange,
            onConfirm = viewModel::saveAccount,
            onDismiss = viewModel::dismissAddEdit
        )
    }
}

@Composable
private fun AccountItem(
    account: CashAccountEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit
) {
    val spacing = LocalResponsiveSpacing.current
    val gradientTheme = KhataGradientPresets.FireOrange

    val typeColor = when (account.type) {
        CashAccountType.CASH -> SuccessGreen
        CashAccountType.BANK -> Color(0xFF1976D2)
        CashAccountType.MOBILE_WALLET -> Color(0xFF7B1FA2)
        CashAccountType.CREDIT_CARD -> Color(0xFFE64A19)
        CashAccountType.OTHER -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    }

    val typeIcon = when (account.type) {
        CashAccountType.CASH -> Icons.Default.AccountBalanceWallet
        CashAccountType.BANK -> Icons.Default.AccountBalance
        CashAccountType.MOBILE_WALLET -> Icons.Default.PhoneAndroid
        CashAccountType.CREDIT_CARD -> Icons.Default.CreditCard
        CashAccountType.OTHER -> Icons.Default.MoreHoriz
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Account icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(typeColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(typeIcon, contentDescription = null, tint = typeColor, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(spacing.itemGap))

            // Account info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (account.isDefault) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = gradientTheme.gradientStart.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Default",
                                style = MaterialTheme.typography.labelSmall,
                                color = gradientTheme.gradientStart,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = account.type.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // Balance
            Text(
                text = account.balance.toCurrency(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // More options
            Box {
                var showMenu by remember { mutableStateOf(false) }
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (!account.isDefault) {
                        DropdownMenuItem(
                            text = { Text("Set as Default") },
                            onClick = { onSetDefault(); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(20.dp)) }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = { onEdit(); showMenu = false },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(20.dp)) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = ErrorRed) },
                        onClick = { onDelete(); showMenu = false },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp), tint = ErrorRed) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditAccountDialog(
    name: String,
    type: CashAccountType,
    isEditing: Boolean,
    error: String?,
    onNameChange: (String) -> Unit,
    onTypeChange: (CashAccountType) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit Account" else "Add Account", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Account Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = error != null,
                    placeholder = { Text("e.g. Counter Cash, HBL Account") }
                )

                // Account type dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = type.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Account Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        CashAccountType.entries.forEach { accountType ->
                            DropdownMenuItem(
                                text = { Text(accountType.displayName) },
                                onClick = {
                                    onTypeChange(accountType)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(if (isEditing) "Update" else "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
