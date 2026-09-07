package com.khatabook.app.ui.screen.suppliers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.khatabook.app.ui.components.SuccessToast
import com.khatabook.app.ui.responsive.*
import com.khatabook.app.ui.language.LocalStrings
import com.khatabook.app.ui.theme.KhataGradientPresets
import com.khatabook.app.util.FormValidator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSupplierScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddSupplierViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val spacing = LocalResponsiveSpacing.current
    val typography = LocalResponsiveTypography.current
    val strings = LocalStrings.current
    val gradientTheme = KhataGradientPresets.FireOrange

    // Inline validation errors
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    val isFormValid = uiState.name.isNotBlank() && uiState.phone.isNotBlank()
            && nameError == null && phoneError == null

    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) {
            SuccessToast.show("Supplier Added")
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.addSupplier, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium.copy(fontSize = typography.h2)) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
                actions = { IconButton(onClick = { viewModel.saveSupplier() }, enabled = !uiState.isSaving) { Icon(Icons.Default.Check, contentDescription = "Save", tint = gradientTheme.gradientStart) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(spacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(spacing.itemGap)
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = {
                    viewModel.onNameChange(it)
                    nameError = FormValidator.name(it)
                },
                label = { Text(strings.supplierName) },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                isError = nameError != null,
                supportingText = nameError?.let { err ->
                    { Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                }
            )
            OutlinedTextField(
                value = uiState.phone,
                onValueChange = {
                    viewModel.onPhoneChange(it)
                    phoneError = FormValidator.phone(it)
                },
                label = { Text(strings.supplierPhone) },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phoneError != null,
                supportingText = phoneError?.let { err ->
                    { Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                }
            )
            OutlinedTextField(value = uiState.shopName, onValueChange = viewModel::onShopNameChange, label = { Text(strings.shopNameOptional) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = uiState.address, onValueChange = viewModel::onAddressChange, label = { Text(strings.addressOptional) }, modifier = Modifier.fillMaxWidth(), maxLines = 2)
            OutlinedTextField(value = uiState.notes, onValueChange = viewModel::onNotesChange, label = { Text(strings.notesOptional) }, modifier = Modifier.fillMaxWidth(), maxLines = 3)

            uiState.error?.let { Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            Spacer(modifier = Modifier.height(spacing.itemGap))

            Button(
                onClick = { viewModel.saveSupplier() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving && isFormValid,
                colors = ButtonDefaults.buttonColors(containerColor = gradientTheme.gradientStart)
            ) {
                if (uiState.isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text(strings.saveSupplier)
            }
        }
    }
}
