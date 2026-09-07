package com.khatabook.app.ui.screen.customers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khatabook.app.data.entity.TransactionEntity
import com.khatabook.app.data.entity.TransactionType
import com.khatabook.app.ui.responsive.*
import com.khatabook.app.ui.theme.ErrorRed
import com.khatabook.app.ui.theme.KhataGradientPresets
import com.khatabook.app.ui.theme.SuccessGreen
import com.khatabook.app.util.PkDateTime
import com.khatabook.app.ui.language.LocalStrings
import com.khatabook.app.util.toCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToNewTransaction: (Long) -> Unit,
    onNavigateToEditTransaction: (Long) -> Unit = {},
    viewModel: CustomerDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val spacing = LocalResponsiveSpacing.current
    val typography = LocalResponsiveTypography.current
    val strings = LocalStrings.current
    val gradientTheme = KhataGradientPresets.FireOrange
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.customer?.name ?: "Customer",
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
                    uiState.customer?.let { customer ->
                        // ═══ Call button — always shown if phone exists ═══
                        if (customer.phone.isNotBlank()) {
                            IconButton(onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${customer.phone}")
                                }
                                context.startActivity(intent)
                            }) {
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = "Call",
                                    tint = SuccessGreen
                                )
                            }
                        }

                        // ═══ WhatsApp button — only shown if whatsappNumber is provided ═══
                        if (!customer.whatsappNumber.isNullOrBlank()) {
                            IconButton(onClick = {
                                val amount = customer.balance.toCurrency()
                                val message = "Aap ka $amount udhaar baaqi hai, bara-e-meharbani ada kar dein"
                                val url = "https://wa.me/${customer.whatsappNumber}?text=${Uri.encode(message)}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            }) {
                                Icon(
                                    Icons.Default.Chat,
                                    contentDescription = "WhatsApp",
                                    tint = androidx.compose.ui.graphics.Color(0xFF25D366)
                                )
                            }
                        }

                        // Share PDF
                        IconButton(onClick = {
                            sharePdfStatement(context, customer, uiState.transactions)
                        }) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share PDF",
                                tint = gradientTheme.gradientStart
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            uiState.customer?.let { customer ->
                if (customer.hasDue) {
                    Box(
                        modifier = Modifier
                            .size(spacing.fabSize)
                            .clip(CircleShape)
                            .background(gradientTheme.gradient),
                        contentAlignment = Alignment.Center
                    ) {
                        FloatingActionButton(
                            onClick = { onNavigateToNewTransaction(customer.id) },
                            containerColor = androidx.compose.ui.graphics.Color.Transparent,
                            contentColor = androidx.compose.ui.graphics.Color.White,
                            elevation = FloatingActionButtonDefaults.elevation(0.dp),
                            modifier = Modifier.size(spacing.fabSize)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "New Transaction",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        uiState.customer?.let { customer ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(spacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(spacing.itemGap)
            ) {
                // ═══ Customer Info Card ═══
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.cardPadding),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // ═══ Photo / Initials Avatar — large & centered ═══
                            CustomerAvatar(
                                customer = customer,
                                size = 120.dp,
                                fontSize = 44.sp
                            )

                            Spacer(modifier = Modifier.height(spacing.itemGap))

                            Text(
                                text = customer.name,
                                style = MaterialTheme.typography.headlineSmall.copy(fontSize = typography.h3),
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Text(
                                text = customer.phone,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = typography.bodyMd),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )

                            Spacer(modifier = Modifier.height(spacing.itemGap))

                            // Balance summary
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                BalanceChip(label = "Credit", amount = customer.totalCredit.toCurrency(), color = ErrorRed)
                                BalanceChip(label = "Payment", amount = customer.totalPayment.toCurrency(), color = SuccessGreen)
                                BalanceChip(label = "Balance", amount = customer.balance.toCurrency(), color = if (customer.hasDue) ErrorRed else SuccessGreen)
                            }

                            // ═══ Action buttons ═══
                            Spacer(modifier = Modifier.height(spacing.itemGap))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(spacing.itemGap)
                            ) {
                                // Call button — always shown
                                if (customer.phone.isNotBlank()) {
                                    OutlinedButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                                data = Uri.parse("tel:${customer.phone}")
                                            }
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SuccessGreen)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(strings.call)
                                    }
                                }

                                // WhatsApp button — only if whatsappNumber is provided
                                if (!customer.whatsappNumber.isNullOrBlank()) {
                                    OutlinedButton(
                                        onClick = {
                                            val amount = customer.balance.toCurrency()
                                            val message = "Aap ka $amount udhaar baaqi hai, bara-e-meharbani ada kar dein"
                                            val url = "https://wa.me/${customer.whatsappNumber}?text=${Uri.encode(message)}"
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = androidx.compose.ui.graphics.Color(0xFF25D366)
                                        )
                                    ) {
                                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(strings.sendWhatsApp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Transaction History
                item {
                    Text(
                        text = "Transaction History",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = typography.h3),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = spacing.itemGap)
                    )
                }

                if (uiState.transactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No transactions yet",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                } else {
                    items(uiState.transactions, key = { it.id }) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onClick = { onNavigateToEditTransaction(transaction.id) }
                        )
                    }
                }
            }
        } ?: run {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    // Delete Confirmation Dialog
    transactionToDelete?.let { txn ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed) },
            title = { Text("Delete Transaction?") },
            text = {
                Text("This will permanently delete this transaction (${txn.amount.toCurrency()}). This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(txn)
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun BalanceChip(label: String, amount: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun TransactionItem(
    transaction: TransactionEntity,
    onClick: () -> Unit
) {
    val spacing = LocalResponsiveSpacing.current
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(spacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (transaction.isPayment || transaction.type == TransactionType.CASH_SALE)
                            SuccessGreen.copy(alpha = 0.12f)
                        else ErrorRed.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (transaction.isPayment || transaction.type == TransactionType.CASH_SALE)
                        Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = if (transaction.isPayment || transaction.type == TransactionType.CASH_SALE)
                        SuccessGreen else ErrorRed,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(spacing.itemGap))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.type.name.replace("_", " ").lowercase()
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = PkDateTime.formatDisplayDate(transaction.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                transaction.description?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Amount + Edit icon
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (transaction.isPayment || transaction.type == TransactionType.CASH_SALE) "+" else "-"}${transaction.amount.toCurrency()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.isPayment || transaction.type == TransactionType.CASH_SALE)
                        SuccessGreen else ErrorRed
                )
                transaction.dueDate?.let { due ->
                    Text(
                        text = "Due: ${PkDateTime.formatDisplayDate(due)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ErrorRed.copy(alpha = 0.7f)
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun sharePdfStatement(
    context: Context,
    customer: com.khatabook.app.data.entity.CustomerEntity,
    transactions: List<TransactionEntity>
) {
    try {
        val statement = buildString {
            appendLine("═══════════════════════════════")
            appendLine("         KHATA ONE")
            appendLine("     Transaction Statement")
            appendLine("═══════════════════════════════")
            appendLine()
            appendLine("Customer: ${customer.name}")
            appendLine("Phone: ${customer.phone}")
            customer.whatsappNumber?.let { appendLine("WhatsApp: $it") }
            appendLine("Date: ${PkDateTime.formatDateTime(PkDateTime.nowUtc())}")
            appendLine()
            appendLine("───────────────────────────────")
            appendLine("Date          Type        Amount")
            appendLine("───────────────────────────────")
            transactions.forEach { txn ->
                val date = PkDateTime.formatDisplayDate(txn.date)
                val type = txn.type.name.replace("_", " ")
                val prefix = if (txn.isPayment) "+" else "-"
                appendLine("$date  ${type.padEnd(10)} $prefix${txn.amount.toCurrency()}")
            }
            appendLine("───────────────────────────────")
            appendLine("Total Credit:  ${customer.totalCredit.toCurrency()}")
            appendLine("Total Payment: ${customer.totalPayment.toCurrency()}")
            appendLine("Balance:       ${customer.balance.toCurrency()}")
            appendLine("═══════════════════════════════")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, statement)
            putExtra(Intent.EXTRA_SUBJECT, "Khata Statement - ${customer.name}")
        }
        context.startActivity(Intent.createChooser(intent, "Share Statement"))
    } catch (e: Exception) {
        Toast.makeText(context, "Error sharing statement", Toast.LENGTH_SHORT).show()
    }
}
