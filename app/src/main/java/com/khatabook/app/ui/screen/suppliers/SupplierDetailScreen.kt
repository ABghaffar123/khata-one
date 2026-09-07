package com.khatabook.app.ui.screen.suppliers

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khatabook.app.data.entity.SupplierTransactionType
import com.khatabook.app.ui.responsive.*
import com.khatabook.app.ui.theme.ErrorRed
import com.khatabook.app.ui.theme.KhataGradientPresets
import com.khatabook.app.ui.theme.SuccessGreen
import com.khatabook.app.util.PkDateTime
import com.khatabook.app.ui.language.LocalStrings
import com.khatabook.app.util.toCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: SupplierDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val spacing = LocalResponsiveSpacing.current
    val typography = LocalResponsiveTypography.current
    val gradientTheme = KhataGradientPresets.FireOrange
    val strings = LocalStrings.current
    var showAddTransaction by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.supplier?.name ?: "Supplier", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    uiState.supplier?.let { supplier ->
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:${supplier.phone}") }
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Default.Phone, contentDescription = "Call", tint = SuccessGreen)
                        }
                        IconButton(onClick = {
                            val message = "Udhaar payment reminder - ${supplier.balance.toCurrency()}"
                            val url = "https://wa.me/${supplier.phone}?text=${Uri.encode(message)}"
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }) {
                            Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = androidx.compose.ui.graphics.Color(0xFF25D366))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTransaction = true },
                containerColor = gradientTheme.gradientStart,
                contentColor = androidx.compose.ui.graphics.Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Transaction")
            }
        }
    ) { padding ->
        uiState.supplier?.let { supplier ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(spacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(spacing.itemGap)
            ) {
                // Supplier Info Card
                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(spacing.cardPadding)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(56.dp).clip(CircleShape).background(gradientTheme.gradient),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(supplier.initials, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White)
                                }
                                Spacer(modifier = Modifier.width(spacing.itemGap))
                                Column {
                                    Text(supplier.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                    Text(supplier.phone, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    supplier.shopName?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) }
                                }
                            }
                            Spacer(modifier = Modifier.height(spacing.itemGap))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                BalanceChip("Udhaar Liya", supplier.totalCreditTaken.toCurrency(), ErrorRed)
                                BalanceChip("Payment Given", supplier.totalPaymentGiven.toCurrency(), SuccessGreen)
                                BalanceChip("Balance", supplier.balance.toCurrency(), if (supplier.hasPayable) ErrorRed else SuccessGreen)
                            }
                            Spacer(modifier = Modifier.height(spacing.itemGap))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.itemGap)) {
                                OutlinedButton(
                                    onClick = { showAddTransaction = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(strings.recordPurchase)
                                }
                                OutlinedButton(
                                    onClick = { showAddTransaction = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SuccessGreen)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(strings.givePayment)
                                }
                            }
                        }
                    }
                }

                item {
                    Text("Transaction History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = spacing.itemGap))
                }

                if (uiState.transactions.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No transactions yet", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        }
                    }
                } else {
                    items(uiState.transactions, key = { it.id }) { txn ->
                        SupplierTransactionItem(txn)
                    }
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }

    // Quick Add Transaction Dialog
    if (showAddTransaction) {
        SupplierQuickAddDialog(
            onDismiss = { showAddTransaction = false },
            onConfirm = { type, amount, desc ->
                viewModel.addTransaction(type, amount, desc)
                showAddTransaction = false
            }
        )
    }
}

@Composable
private fun BalanceChip(label: String, amount: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        Text(amount, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun SupplierTransactionItem(txn: com.khatabook.app.data.entity.SupplierTransactionEntity) {
    val spacing = LocalResponsiveSpacing.current
    val dateFormat = remember { "dd MMM yyyy" }

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(spacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                    .background(if (txn.isPurchase) ErrorRed.copy(alpha = 0.12f) else SuccessGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (txn.isPurchase) Icons.Default.ShoppingCart else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (txn.isPurchase) ErrorRed else SuccessGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(spacing.itemGap))
            Column(modifier = Modifier.weight(1f)) {
                Text(if (txn.isPurchase) "Purchase" else "Payment Given", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(PkDateTime.formatDisplayDate(txn.date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                txn.description?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) }
            }
            Text(
                "${if (txn.isPurchase) "+" else "-"}${txn.amount.toCurrency()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (txn.isPurchase) ErrorRed else SuccessGreen
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SupplierQuickAddDialog(onDismiss: () -> Unit, onConfirm: (SupplierTransactionType, Double, String?) -> Unit) {
    var type by remember { mutableStateOf(SupplierTransactionType.PURCHASE) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val gradientTheme = KhataGradientPresets.FireOrange

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.recordTransaction) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = type == SupplierTransactionType.PURCHASE, onClick = { type = SupplierTransactionType.PURCHASE }, label = { Text(strings.purchase) })
                    FilterChip(selected = type == SupplierTransactionType.PAYMENT, onClick = { type = SupplierTransactionType.PAYMENT }, label = { Text(strings.payment) })
                }
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text(strings.amountRs) }, singleLine = true)
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text(strings.descriptionOptional) }, maxLines = 2)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val a = amount.toDoubleOrNull() ?: return@Button
                    onConfirm(type, a, description.ifBlank { null })
                },
                colors = ButtonDefaults.buttonColors(containerColor = gradientTheme.gradientStart)
            ) { Text(strings.save) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } }
    )
}
