package com.khatabook.app.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khatabook.app.ui.responsive.*
import com.khatabook.app.ui.screen.customers.CustomerAvatar
import com.khatabook.app.ui.theme.ErrorRed
import com.khatabook.app.ui.theme.KhataGradientPresets
import com.khatabook.app.ui.theme.SuccessGreen
import com.khatabook.app.util.getGreeting
import com.khatabook.app.util.toCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCustomers: () -> Unit = {},
    onNavigateToNewEntry: () -> Unit = {},
    onNavigateToScan: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToDailySummary: () -> Unit = {},
    onNavigateToDueCustomers: () -> Unit = {},
    onNavigateToLowStock: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val windowSize = LocalWindowSize.current
    val spacing = LocalResponsiveSpacing.current
    val typography = LocalResponsiveTypography.current
    val gradientTheme = KhataGradientPresets.FireOrange

    var showNotificationSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = getGreeting(), style = MaterialTheme.typography.bodyMedium.copy(fontSize = typography.bodyMd), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(text = "Khata One", style = MaterialTheme.typography.headlineMedium.copy(fontSize = typography.h2), fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = { }) { Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(spacing.buttonIconSize)) }

                    // ═══ Unified Notification Bell ═══
                    Box {
                        IconButton(onClick = { showNotificationSheet = true }) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                modifier = Modifier.size(spacing.buttonIconSize)
                            )
                        }
                        // Badge showing combined count of all notification types
                        val badgeCount = uiState.notificationBadgeCount
                        if (badgeCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(ErrorRed)
                                    .align(Alignment.TopEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (badgeCount > 9) "9+" else "$badgeCount",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            Box(
                modifier = Modifier.size(spacing.fabSize).shadow(elevation = 8.dp, shape = CircleShape, ambientColor = gradientTheme.gradientStart.copy(alpha = 0.3f), spotColor = gradientTheme.gradientStart.copy(alpha = 0.4f)).clip(CircleShape).background(gradientTheme.gradient),
                contentAlignment = Alignment.Center
            ) {
                FloatingActionButton(
                    onClick = onNavigateToNewEntry,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp),
                    modifier = Modifier.size(spacing.fabSize)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Entry", modifier = Modifier.size(spacing.buttonIconSize + 4.dp))
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(spacing.sectionGap)
        ) {
            // Stats Cards
            item {
                MaxWidthContainer {
                    StatsRow {
                        Box(modifier = Modifier.weight(1f)) { ResponsiveStatCard("Total Dues", uiState.totalDues.toCurrency(), Icons.Default.ArrowUpward, ErrorRed, MaterialTheme.colorScheme.errorContainer) }
                        Box(modifier = Modifier.weight(1f)) { ResponsiveStatCard("Today", uiState.todayCollected.toCurrency(), Icons.Default.ArrowDownward, SuccessGreen, MaterialTheme.colorScheme.secondaryContainer) }
                        if (windowSize.widthSizeClass >= WindowWidthSizeClass.Expanded) {
                            Box(modifier = Modifier.weight(1f)) { ResponsiveStatCard("This Month", uiState.thisMonthTotal.toCurrency(), Icons.Default.ArrowDownward, gradientTheme.gradientStart, MaterialTheme.colorScheme.primaryContainer) }
                        }
                    }
                }
            }

            // Quick Actions
            item {
                MaxWidthContainer {
                    ResponsiveSection(title = "Quick Actions") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.actionCardGap)) {
                            Box(modifier = Modifier.weight(1f)) { ResponsiveActionCard("New Entry", Icons.Default.Add, onNavigateToNewEntry) }
                            Box(modifier = Modifier.weight(1f)) { ResponsiveActionCard("Customers", Icons.Default.Search, onNavigateToCustomers) }
                            Box(modifier = Modifier.weight(1f)) { ResponsiveActionCard("Reports", Icons.Default.BarChart, onNavigateToReports) }
                        }
                    }
                }
            }

            // Top Debtors
            item {
                MaxWidthContainer {
                    ResponsiveSection(title = "Top Debtors", actionText = "View All", onAction = onNavigateToCustomers) {
                        if (uiState.topDebtors.isEmpty()) {
                            ResponsiveCard {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("No pending dues", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(spacing.itemGap)) {
                                uiState.topDebtors.forEach { debtor ->
                                    ResponsiveCard {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(spacing.cardPadding),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                CustomerAvatar(customer = debtor, size = 40.dp, fontSize = 14.sp)
                                                Spacer(modifier = Modifier.width(spacing.itemGap))
                                                Text(
                                                    text = debtor.name,
                                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = typography.bodyLg),
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Text(
                                                text = debtor.balance.toCurrency(),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = ErrorRed
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Recent Activity
            item {
                MaxWidthContainer {
                    ResponsiveSection(title = "Recent Activity") {
                        if (uiState.recentTransactions.isEmpty()) {
                            ResponsiveCard {
                                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("No transactions yet", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(spacing.itemGap)) {
                                uiState.recentTransactions.take(5).forEach { txn ->
                                    val isPayment = txn.isPayment || txn.type == com.khatabook.app.data.entity.TransactionType.CASH_SALE
                                    ResponsiveCard {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            // Customer avatar with a small payment/credit badge
                                            val customer = viewModel.customerById(txn.customerId)
                                            if (customer != null) {
                                                Box {
                                                    CustomerAvatar(customer = customer, size = 40.dp, fontSize = 14.sp)
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.BottomEnd)
                                                            .size(18.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isPayment) SuccessGreen else ErrorRed),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            if (isPayment) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(11.dp)
                                                        )
                                                    }
                                                }
                                            } else {
                                                Box(
                                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(if (isPayment) SuccessGreen.copy(alpha = 0.12f) else ErrorRed.copy(alpha = 0.12f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(if (isPayment) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward, contentDescription = null, tint = if (isPayment) SuccessGreen else ErrorRed, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("${if (isPayment) "Received" else "Credit"} ${txn.customerName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                            Text("${if (isPayment) "+" else "-"}${txn.amount.toCurrency()}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (isPayment) SuccessGreen else ErrorRed)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(spacing.fabSize + spacing.screenVertical)) }
        }
    }

    // ═══ Unified Notification Bottom Sheet ═══
    if (showNotificationSheet) {
        NotificationBottomSheet(
            notifications = uiState.notifications,
            onDismiss = { showNotificationSheet = false },
            onNavigateToDailySummary = {
                showNotificationSheet = false
                onNavigateToDailySummary()
            },
            onNavigateToDueCustomers = {
                showNotificationSheet = false
                onNavigateToDueCustomers()
            },
            onNavigateToLowStock = {
                showNotificationSheet = false
                onNavigateToLowStock()
            },
            onDismissNotification = { notificationId ->
                viewModel.dismissNotification(notificationId)
            }
        )
    }
}
