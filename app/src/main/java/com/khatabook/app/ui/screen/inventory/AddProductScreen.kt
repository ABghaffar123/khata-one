package com.khatabook.app.ui.screen.inventory

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khatabook.app.data.entity.ProductUnit
import com.khatabook.app.ui.components.SuccessToast
import com.khatabook.app.ui.responsive.*
import com.khatabook.app.ui.theme.ErrorRed
import com.khatabook.app.ui.theme.KhataGradientPresets
import com.khatabook.app.ui.theme.SuccessGreen
import com.khatabook.app.ui.language.LocalStrings
import com.khatabook.app.util.FormValidator
import com.khatabook.app.util.toCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    onNavigateBack: () -> Unit,
    onNavigateToScanBarcode: () -> Unit = {},
    viewModel: AddProductViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val spacing = LocalResponsiveSpacing.current
    val typography = LocalResponsiveTypography.current
    val strings = LocalStrings.current
    val gradientTheme = KhataGradientPresets.FireOrange

    // Inline validation errors
    var nameError by remember { mutableStateOf<String?>(null) }
    var salePriceError by remember { mutableStateOf<String?>(null) }
    var purchasePriceError by remember { mutableStateOf<String?>(null) }
    var stockError by remember { mutableStateOf<String?>(null) }
    var thresholdError by remember { mutableStateOf<String?>(null) }
    val isFormValid = uiState.name.isNotBlank() && nameError == null
            && salePriceError == null && purchasePriceError == null
            && stockError == null && thresholdError == null

    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) {
            SuccessToast.show("Product Saved")
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.addProduct,
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
                    IconButton(onClick = { viewModel.saveProduct() }, enabled = !uiState.isSaving) {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = gradientTheme.gradientStart)
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
            // Product Name
            OutlinedTextField(
                value = uiState.name,
                onValueChange = {
                    viewModel.onNameChange(it)
                    nameError = FormValidator.productName(it)
                },
                label = { Text(strings.productName) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nameError != null,
                supportingText = nameError?.let { err ->
                    { Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                }
            )

            // Barcode field with scan button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.barcode,
                    onValueChange = viewModel::onBarcodeChange,
                    label = { Text(strings.barcodeOptional) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = gradientTheme.gradientStart
                        )
                    },
                    trailingIcon = {
                        if (uiState.barcode.isNotBlank()) {
                            IconButton(onClick = { viewModel.onBarcodeChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    }
                )
                FilledTonalButton(
                    onClick = onNavigateToScanBarcode,
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Barcode status indicator
            if (uiState.isDuplicateCheck) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Text(
                        text = strings.checkingBarcode,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            // Unit selector
            Text(strings.unit, style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProductUnit.values().take(6).forEach { unit ->
                    FilterChip(
                        selected = uiState.unit == unit,
                        onClick = { viewModel.onUnitChange(unit) },
                        label = { Text(unit.displayName, style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProductUnit.values().drop(6).forEach { unit ->
                    FilterChip(
                        selected = uiState.unit == unit,
                        onClick = { viewModel.onUnitChange(unit) },
                        label = { Text(unit.displayName, style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }

            OutlinedTextField(
                value = uiState.salePrice,
                onValueChange = {
                    viewModel.onSalePriceChange(it)
                    salePriceError = FormValidator.price(it, required = false)
                },
                label = { Text(strings.salePriceRs) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = salePriceError != null,
                supportingText = salePriceError?.let { err ->
                    { Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                }
            )

            OutlinedTextField(
                value = uiState.purchasePrice,
                onValueChange = {
                    viewModel.onPurchasePriceChange(it)
                    purchasePriceError = FormValidator.price(it, required = false)
                },
                label = { Text(strings.purchasePriceRs) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = purchasePriceError != null,
                supportingText = purchasePriceError?.let { err ->
                    { Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                }
            )

            OutlinedTextField(
                value = uiState.stockQuantity,
                onValueChange = {
                    viewModel.onStockQuantityChange(it)
                    stockError = FormValidator.quantity(it, allowDecimal = true)
                },
                label = { Text(strings.stockQuantity) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = stockError != null,
                supportingText = stockError?.let { err ->
                    { Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                }
            )

            OutlinedTextField(
                value = uiState.lowStockThreshold,
                onValueChange = {
                    viewModel.onLowStockThresholdChange(it)
                    thresholdError = FormValidator.quantity(it, allowDecimal = false)
                },
                label = { Text(strings.lowStockAlertThreshold) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = thresholdError != null,
                supportingText = thresholdError?.let { err ->
                    { Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                }
            )

            uiState.error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(spacing.itemGap))

            Button(
                onClick = { viewModel.saveProduct() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving && isFormValid,
                colors = ButtonDefaults.buttonColors(containerColor = gradientTheme.gradientStart)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(strings.saveProduct)
                }
            }
        }
    }

    // ═══ Duplicate Product Dialog ═══
    if (uiState.showDuplicateDialog && uiState.existingProduct != null) {
        val existing = uiState.existingProduct!!
        var additionalQty by remember { mutableStateOf("1") }

        AlertDialog(
            onDismissRequest = { viewModel.onDuplicateDismiss() },
            icon = {
                Icon(
                    Icons.Default.Inventory,
                    contentDescription = null,
                    tint = gradientTheme.gradientStart,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = strings.productAlreadyExists,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = existing.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (existing.barcode.isNotBlank()) {
                                Text(
                                    text = "Barcode: ${existing.barcode}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = strings.currentStock,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = "${existing.stockQuantity} ${existing.unit.displayName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = strings.salePrice,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = existing.salePrice.toCurrency(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = SuccessGreen
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = strings.addStockInstead,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    OutlinedTextField(
                        value = additionalQty,
                        onValueChange = { additionalQty = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text(strings.additionalQuantity) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = additionalQty.toDoubleOrNull() ?: 1.0
                        viewModel.onDuplicateConfirmUpdate(qty)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SuccessGreen
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.updateStock)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.onDuplicateDismiss() },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = gradientTheme.gradientStart
                    )
                ) {
                    Text(strings.createNew)
                }
            }
        )
    }
}
