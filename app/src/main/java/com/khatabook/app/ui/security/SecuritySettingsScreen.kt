package com.khatabook.app.ui.security

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ═══════════════════════════════════════════════════════════════════
 * SECURITY SETTINGS SCREEN — Premium security configuration page
 * ═══════════════════════════════════════════════════════════════════
 *
 * Layout:
 *   1. Security Status Overview (3 indicator cards)
 *   2. Privacy Score Card (checklist + progress)
 *   3. PIN Lock Card
 *   4. Biometric Lock Card
 *   5. Auto-Lock Card
 *   6. Backup Encryption Card
 *   7. Session Security Card
 *   8. Danger Zone Card
 *
 * Supports: English, Urdu, Urdu Roman
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    viewModel: SecurityViewModel,
    onBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Security Settings",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // ═══════════════════════════════════════════════════
            // 1. SECURITY STATUS OVERVIEW
            // ═══════════════════════════════════════════════════
            SecurityStatusCards(
                pinStatus = state.pinStatus,
                biometricStatus = state.biometricStatusType,
                encryptionStatus = state.encryptionStatusType
            )

            // ═══════════════════════════════════════════════════
            // 2. PRIVACY SCORE
            // ═══════════════════════════════════════════════════
            PrivacyScoreCard(
                score = state.securityScore,
                level = state.securityLevel,
                isPinEnabled = state.isPinEnabled,
                isBiometricEnabled = state.isBiometricEnabled,
                isAutoLockEnabled = state.isAutoLockEnabled,
                isEncryptionEnabled = state.isEncryptionEnabled
            )

            // Section divider
            SectionHeader(title = "App Lock")

            // ═══════════════════════════════════════════════════
            // 3. PIN LOCK
            // ═══════════════════════════════════════════════════
            PinLockCard(
                isEnabled = state.isPinEnabled,
                pinDots = state.pinDots,
                onToggle = { viewModel.togglePinLock(it) },
                onChangePin = { viewModel.changePin() },
                onRemovePin = { viewModel.removePin() }
            )

            // ═══════════════════════════════════════════════════
            // 4. BIOMETRIC LOCK
            // ═══════════════════════════════════════════════════
            BiometricLockCard(
                isEnabled = state.isBiometricEnabled,
                isAvailable = state.biometricAvailable,
                savedFingerprints = state.savedFingerprints,
                onToggle = { viewModel.toggleBiometric(it) },
                onTest = { viewModel.testBiometric() }
            )

            // Section divider
            SectionHeader(title = "Auto-Lock")

            // ═══════════════════════════════════════════════════
            // 5. AUTO-LOCK
            // ═══════════════════════════════════════════════════
            AutoLockCard(
                isEnabled = state.isAutoLockEnabled,
                selectedDuration = state.autoLockDuration,
                onToggle = { viewModel.toggleAutoLock(it) },
                onDurationSelected = { viewModel.setAutoLockDuration(it) }
            )

            // Section divider
            SectionHeader(title = "Data Protection")

            // ═══════════════════════════════════════════════════
            // 6. BACKUP ENCRYPTION
            // ═══════════════════════════════════════════════════
            BackupEncryptionCard(
                isEnabled = state.isEncryptionEnabled,
                algorithm = state.encryptionAlgorithm,
                keyLength = state.keyLength,
                lastBackupTime = state.lastBackupTime,
                onToggle = { viewModel.toggleEncryption(it) },
                onChangePassword = { viewModel.changeEncryptionPassword() },
                onViewHistory = { /* Navigate to backup history */ }
            )

            // ═══════════════════════════════════════════════════
            // 7. SESSION SECURITY
            // ═══════════════════════════════════════════════════
            SessionSecurityCard(
                clearOnFailedAttempts = state.clearOnFailedAttempts,
                hideInBackground = state.hideInBackground,
                blockScreenshots = state.blockScreenshots,
                onToggleClearOnFailed = { viewModel.toggleClearOnFailed(it) },
                onToggleHideInBackground = { viewModel.toggleHideInBackground(it) },
                onToggleBlockScreenshots = { viewModel.toggleBlockScreenshots(it) }
            )

            // ═══════════════════════════════════════════════════
            // 8. DANGER ZONE
            // ═══════════════════════════════════════════════════
            DangerZoneCard(
                onRemoveSecurity = { viewModel.removeAllSecurity() },
                onResetData = { viewModel.resetAppData() }
            )

            // Footer
            Text(
                text = "Made with ❤️ for Pakistani Shopkeepers",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
                    .wrapContentWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.padding(top = 4.dp)
    )
}
