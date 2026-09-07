package com.khatabook.app.ui.screen.customers

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.khatabook.app.ui.components.SuccessToast
import com.khatabook.app.ui.responsive.*
import com.khatabook.app.ui.language.LocalStrings
import com.khatabook.app.ui.theme.KhataGradientPresets
import com.khatabook.app.util.CustomerPhotoProcessor
import com.khatabook.app.util.FormValidator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddCustomerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val spacing = LocalResponsiveSpacing.current
    val typography = LocalResponsiveTypography.current
    val strings = LocalStrings.current
    val gradientTheme = KhataGradientPresets.FireOrange
    val context = LocalContext.current

    // ═══ Photo flow: pick → circular crop → compress & persist ═══
    // Track camera temp files so they can be cleaned up after cropping.
    var cameraTempFile by remember { mutableStateOf<String?>(null) }

    // uCrop circular crop step (zoom, drag, Cancel / Save built in)
    val cropLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { result ->
        if (result.isSuccessful && result.uriContent != null) {
            // Copy the cropped result out of the cache into permanent storage,
            // resized to ≤300×300 and compressed to <100 KB.
            val savedPath = CustomerPhotoProcessor.importPhoto(context, result.uriContent)
            if (savedPath != null) {
                CustomerPhotoProcessor.deletePhoto(uiState.photoUri)
                viewModel.onPhotoUriChange(savedPath)
            }
        }
        // Clean up camera temp file (if any) after crop completes or is cancelled
        cameraTempFile?.let { tmp ->
            runCatching { java.io.File(tmp).delete() }
            cameraTempFile = null
        }
    }

    fun launchCrop(source: Uri) {
        cropLauncher.launch(
            CropImageContractOptions(
                uri = source,
                cropImageOptions = CropImageOptions(
                    cropShape = CropImageView.CropShape.OVAL,
                    outputCompressFormat = android.graphics.Bitmap.CompressFormat.PNG,
                    outputCompressQuality = 100
                )
            )
        )
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { launchCrop(it) }
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            // Save the preview to a temp file the cropper can read
            val bytes = java.io.ByteArrayOutputStream()
            it.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, bytes)
            val file = java.io.File(context.filesDir, "customer_photo_tmp_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { out -> bytes.toByteArray().inputStream().copyTo(out) }
            cameraTempFile = file.absolutePath
            launchCrop(Uri.fromFile(file))
        }
    }

    // Inline validation errors
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    val isFormValid = uiState.name.isNotBlank() && uiState.phone.isNotBlank()
            && nameError == null && phoneError == null

    // WhatsApp same-as-phone toggle
    val isWhatsappSameAsPhone by remember(uiState.sameAsPhone) { mutableStateOf(uiState.sameAsPhone) }

    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) {
            SuccessToast.show("Customer Added")
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.addCustomer,
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
                    IconButton(
                        onClick = { viewModel.saveCustomer() },
                        enabled = !uiState.isSaving
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Save",
                            tint = gradientTheme.gradientStart
                        )
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
            verticalArrangement = Arrangement.spacedBy(spacing.itemGap),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ═══ Photo Section ═══
            Box(
                modifier = Modifier
                    .padding(top = spacing.itemGap)
                    .clickable { /* Show photo picker options */ },
                contentAlignment = Alignment.BottomEnd
            ) {
                // Photo or initials avatar
                val currentPhotoUri = uiState.photoUri?.let { Uri.parse(it) }
                if (currentPhotoUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(currentPhotoUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Customer Photo",
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .border(3.dp, gradientTheme.gradientStart, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(gradientTheme.gradientStart.copy(alpha = 0.12f))
                            .border(2.dp, gradientTheme.gradientStart.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.name.ifBlank { "?" }.let { name ->
                                val parts = name.split(" ").filter { it.isNotBlank() }
                                when {
                                    parts.isEmpty() -> "?"
                                    parts.size == 1 -> parts[0].first().uppercase()
                                    else -> "${parts[0].first()}${parts[1].first()}".uppercase()
                                }
                            },
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = gradientTheme.gradientStart
                        )
                    }
                }

                // Camera icon button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(gradientTheme.gradientStart)
                        .clickable {
                            // Show photo options dialog
                            showPhotoOptionsDialog = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Add Photo",
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Photo options dialog
            var showPhotoOptionsDialog by remember { mutableStateOf(false) }
            if (showPhotoOptionsDialog) {
                AlertDialog(
                    onDismissRequest = { showPhotoOptionsDialog = false },
                    title = { Text("Add Photo") },
                    text = { Text("Choose how to add a photo") },
                    confirmButton = {
                        TextButton(onClick = {
                            showPhotoOptionsDialog = false
                            galleryLauncher.launch("image/*")
                        }) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gallery")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showPhotoOptionsDialog = false
                            cameraLauncher.launch(null)
                        }) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Camera")
                        }
                    }
                )
            }

            // Name field
            OutlinedTextField(
                value = uiState.name,
                onValueChange = {
                    viewModel.onNameChange(it)
                    nameError = FormValidator.name(it)
                },
                label = { Text(strings.customerName) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nameError != null,
                supportingText = nameError?.let { err ->
                    { Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                }
            )

            // Phone Number field (required)
            OutlinedTextField(
                value = uiState.phone,
                onValueChange = {
                    viewModel.onPhoneChange(it)
                    phoneError = FormValidator.phone(it)
                },
                label = { Text("Phone Number *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(20.dp)) },
                isError = phoneError != null,
                supportingText = phoneError?.let { err ->
                    { Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                }
            )

            // WhatsApp Number toggle + field
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = null,
                            tint = androidx.compose.ui.graphics.Color(0xFF25D366),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "WhatsApp Number (Optional)",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Same as Phone toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = uiState.sameAsPhone,
                            onCheckedChange = { viewModel.onSameAsPhoneChange(it) },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = androidx.compose.ui.graphics.Color(0xFF25D366),
                                checkedThumbColor = androidx.compose.ui.graphics.Color.White,
                                uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Same as Phone Number",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Turn on if this customer uses the same number for WhatsApp",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }

                    // WhatsApp number input — auto-filled & read-only when same as phone
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = if (uiState.sameAsPhone) uiState.phone else uiState.whatsappNumber,
                        onValueChange = viewModel::onWhatsappNumberChange,
                        label = { Text("WhatsApp Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !uiState.sameAsPhone,
                        readOnly = uiState.sameAsPhone,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = { Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(20.dp)) }
                    )
                }
            }

            // Address field
            OutlinedTextField(
                value = uiState.address,
                onValueChange = viewModel::onAddressChange,
                label = { Text(strings.addressOptional) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            // Notes field
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text(strings.notesOptional) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(spacing.itemGap))

            Button(
                onClick = { viewModel.saveCustomer() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving && isFormValid,
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
                    Text(strings.saveCustomer)
                }
            }
        }
    }
}
