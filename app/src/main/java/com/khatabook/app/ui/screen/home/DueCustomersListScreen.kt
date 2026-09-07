package com.khatabook.app.ui.screen.home

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khatabook.app.ui.responsive.*
import com.khatabook.app.ui.screen.customers.CustomerAvatar
import com.khatabook.app.ui.theme.ErrorRed
import com.khatabook.app.ui.theme.KhataGradientPresets
import com.khatabook.app.ui.theme.SuccessGreen
import com.khatabook.app.util.toCurrency
import com.khatabook.app.util.PkDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DueCustomersListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCustomerDetail: (Long) -> Unit,
    viewModel: DuesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val spacing = LocalResponsiveSpacing.current
    val typography = LocalResponsiveTypography.current
    val gradientTheme = KhataGradientPresets.FireOrange

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Customer Dues",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = typography.h2
                            ),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.totalOverdueCount} customers • ${uiState.totalOverdueAmount.toCurrency()} total",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
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
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.dueCustomers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = SuccessGreen.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "All Clear!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No customers have pending dues",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(spacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(spacing.itemGap)
            ) {
                items(uiState.dueCustomers, key = { it.customer.id }) { dueCustomer ->
                    DueCustomerCard(
                        dueCustomer = dueCustomer,
                        gradientTheme = gradientTheme,
                        onClick = { onNavigateToCustomerDetail(dueCustomer.customer.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DueCustomerCard(
    dueCustomer: DueCustomer,
    gradientTheme: com.khatabook.app.ui.theme.AppGradientTheme,
    onClick: () -> Unit
) {
    val spacing = LocalResponsiveSpacing.current
    val isOverdue = dueCustomer.daysOverdue > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Customer avatar — photo (small) or initials fallback
            CustomerAvatar(customer = dueCustomer.customer, size = 48.dp)

            Spacer(modifier = Modifier.width(spacing.itemGap))

            // Customer info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dueCustomer.customer.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = dueCustomer.customer.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (dueCustomer.dueDate != null) {
                        Icon(
                            Icons.Default.Event,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isOverdue) ErrorRed
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = PkDateTime.formatDisplayDate(dueCustomer.dueDate),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOverdue) ErrorRed
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Due amount + overdue badge
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = dueCustomer.dueAmount.toCurrency(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isOverdue) ErrorRed else gradientTheme.gradientStart
                )
                if (isOverdue) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = ErrorRed.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${dueCustomer.daysOverdue}d overdue",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = ErrorRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else if (dueCustomer.dueDate != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Due soon",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
