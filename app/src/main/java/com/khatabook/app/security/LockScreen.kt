package com.khatabook.app.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Full-screen lock screen with PIN number pad and biometric fallback.
 *
 * - Shows 4-digit PIN dots
 * - Number pad for PIN entry
 * - Auto-verifies on 4th digit
 * - Biometric button (fingerprint icon) if available
 * - After 3 biometric failures, forces PIN only
 */
@Composable
fun LockScreen(
    securityManager: SecurityManager,
    onUnlocked: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPinPad by remember { mutableStateOf(false) }
    var biometricBlocked by remember { mutableStateOf(securityManager.isBiometricBlocked) }
    val context = LocalContext.current

    // Try biometric on launch
    LaunchedEffect(Unit) {
        if (securityManager.isBiometricEnabled && !biometricBlocked) {
            showBiometric(context, securityManager, onUnlocked) { success ->
                if (!success) {
                    // biometric failed, show PIN pad
                    showPinPad = true
                }
            }
        } else {
            showPinPad = true
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Khata One",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (biometricBlocked) "Enter PIN to unlock"
                       else "Verify identity to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (showPinPad || biometricBlocked) {
                // PIN Dots
                PinDots(pin = pin, primaryColor = primaryColor)

                Spacer(modifier = Modifier.height(16.dp))

                // Error message
                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.W500
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Number Pad
                NumberPad(
                    onKeyPress = { key ->
                        when (key) {
                            "⌫" -> {
                                if (pin.isNotEmpty()) {
                                    pin = pin.dropLast(1)
                                }
                            }
                            else -> {
                                if (pin.length < 4) {
                                    pin += key
                                    if (pin.length == 4) {
                                        // Auto-verify
                                        val success = securityManager.verifyPin(pin)
                                        if (success) {
                                            securityManager.resetBiometricFailures()
                                            onUnlocked()
                                        } else {
                                            pin = ""
                                            errorMessage = "Wrong PIN. Try again."
                                        }
                                    }
                                }
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Biometric button
                if (!biometricBlocked && securityManager.isBiometricEnabled) {
                    TextButton(
                        onClick = {
                            showBiometric(context, securityManager, onUnlocked) { success ->
                                if (!success) {
                                    val blocked = securityManager.recordBiometricFailure()
                                    if (blocked) {
                                        biometricBlocked = true
                                        errorMessage = "Too many failed attempts. Please enter PIN."
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Fingerprint,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Use Biometric")
                    }
                }
            } else {
                // Loading while biometric is attempting
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Authenticating...")
            }
        }
    }
}

@Composable
private fun PinDots(pin: String, primaryColor: Color) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        repeat(4) { index ->
            val filled = index < pin.length
            val color = if (filled) primaryColor
                       else MaterialTheme.colorScheme.outlineVariant

            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                if (filled) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
        }
    }
}

@Composable
private fun NumberPad(onKeyPress: (String) -> Unit) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "⌫")
    )

    Column {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    if (key.isEmpty()) {
                        Spacer(modifier = Modifier.size(72.dp, 56.dp))
                    } else {
                        Surface(
                            modifier = Modifier
                                .size(72.dp, 56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onKeyPress(key) },
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (key == "⌫") {
                                    Icon(
                                        Icons.Default.Backspace,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                } else {
                                    Text(
                                        text = key,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * Launches the system biometric prompt.
 * Calls [onResult] with true if authenticated, false otherwise.
 */
private fun showBiometric(
    context: Context,
    securityManager: SecurityManager,
    onUnlocked: () -> Unit,
    onResult: (Boolean) -> Unit
) {
    val activity = context as? FragmentActivity ?: run {
        onResult(false)
        return
    }

    val biometricManager = BiometricManager.from(context)
    if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL) != BiometricManager.BIOMETRIC_SUCCESS) {
        onResult(false)
        return
    }

    val executor = ContextCompat.getMainExecutor(context)
    val biometricPrompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                securityManager.unlock()
                onUnlocked()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onResult(false)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onResult(false)
            }
        }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Unlock Khata One")
        .setSubtitle("Verify your identity")
        .setNegativeButtonText("Use PIN")
        .build()

    biometricPrompt.authenticate(promptInfo)
}
