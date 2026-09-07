package com.khatabook.app.ui.screen.suppliers

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khatabook.app.data.entity.SupplierEntity
import com.khatabook.app.ui.responsive.*
import com.khatabook.app.ui.theme.ErrorRed
import com.khatabook.app.ui.theme.KhataGradientPresets
import com.khatabook.app.ui.theme.SuccessGreen
import com.khatabook.app.ui.language.LocalStrings
import com.khatabook.app.util.toCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuppliersScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAdd: () -> Unit,
    viewModel: SupplierListViewModel = hiltViewModel()
) {
    val suppliers by viewModel.suppliers.collectAsStateWithLifecycle()
    val totalPayables by viewModel.totalPayables.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val spacing = LocalResponsiveSpacing.current
    val typography = LocalResponsiveTypography.current
    val strings = LocalStrings.current
    val gradientTheme = KhataGradientPresets.FireOrange

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.suppliers,
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = typography.h2),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .size(spacing.fabSize)
                    .clip(CircleShape)
                    .background(gradientTheme.gradient)
                    .clickable { onNavigateToAdd() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Supplier", tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(28.dp))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(spacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(spacing.itemGap)
        ) {
            // Total Payables Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Total Payables", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text(
                                (totalPayables ?: 0.0).toCurrency(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = ErrorRed
                            )
                        }
                    }
                }
            }

            // Search
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(strings.searchSuppliers) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large
                )
            }

            if (suppliers.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No suppliers yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap + to add your first supplier",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            } else {
                items(suppliers, key = { it.id }) { supplier ->
                    SupplierListItem(
                        supplier = supplier,
                        onClick = { onNavigateToDetail(supplier.id) },
                        onDelete = { viewModel.deleteSupplier(supplier) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SupplierListItem(supplier: SupplierEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    val spacing = LocalResponsiveSpacing.current
    val gradientTheme = KhataGradientPresets.FireOrange
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { if (it == SwipeToDismissBoxValue.EndToStart) { onDelete(); true } else false }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (dismissState.dismissValue == SwipeToDismissBoxValue.EndToStart) ErrorRed else MaterialTheme.colorScheme.surface,
                label = "swipe"
            )
            Box(modifier = Modifier.fillMaxSize().background(color).padding(horizontal = 20.dp), contentAlignment = Alignment.CenterEnd) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = androidx.compose.ui.graphics.Color.White)
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(spacing.cardPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(gradientTheme.gradientStart.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(supplier.initials, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = gradientTheme.gradientStart)
                }
                Spacer(modifier = Modifier.width(spacing.itemGap))
                Column(modifier = Modifier.weight(1f)) {
                    Text(supplier.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(supplier.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(supplier.balance.toCurrency(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (supplier.hasPayable) ErrorRed else SuccessGreen)
                    Text(if (supplier.hasPayable) "Payable" else "Settled", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        }
    }
}
