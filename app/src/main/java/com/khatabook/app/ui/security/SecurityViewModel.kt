package com.khatabook.app.ui.security

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ═══════════════════════════════════════════════════════════════════
 * SECURITY VIEW MODEL — Manages all security settings state
 * ═══════════════════════════════════════════════════════════════════
 *
 * Sections:
 *   1. PIN Lock — 4-6 digit PIN
 *   2. Biometric Lock — Fingerprint / Face
 *   3. Auto-Lock — Timeout duration
 *   4. Backup Encryption — AES-256 password
 *   5. Session Security — Extra protections
 *   6. Danger Zone — Reset / Remove
 */
class SecurityViewModel : ViewModel() {

    // ═══════════════════════════════════════════════════════════════
    // UI STATE
    // ═══════════════════════════════════════════════════════════════

    data class SecurityState(
        // PIN Lock
        val isPinEnabled: Boolean = false,
        val pinLength: Int = 0,              // 0 = no PIN set, 4-6 = length
        val pinDots: List<Boolean> = List(4) { false }, // Visual dots

        // Biometric
        val isBiometricEnabled: Boolean = false,
        val biometricAvailable: Boolean = true,   // Device supports it
        val savedFingerprints: Int = 0,
        val biometricStatus: BiometricStatus = BiometricStatus.NOT_CONFIGURED,

        // Auto-Lock
        val isAutoLockEnabled: Boolean = false,
        val autoLockDuration: AutoLockDuration = AutoLockDuration.ONE_MINUTE,

        // Backup Encryption
        val isEncryptionEnabled: Boolean = false,
        val encryptionAlgorithm: String = "AES-256-GCM",
        val keyLength: String = "256-bit",
        val lastBackupTime: String = "Not yet",
        val encryptionStatus: EncryptionStatus = EncryptionStatus.NOT_CONFIGURED,

        // Session Security
        val clearOnFailedAttempts: Boolean = false,
        val hideInBackground: Boolean = false,
        val blockScreenshots: Boolean = false,

        // Overall
        val language: String = "en"
    ) {
        /**
         * Calculate security score (0-100).
         */
        val securityScore: Int
            get() {
                var score = 0
                if (isPinEnabled) score += 30
                if (isBiometricEnabled) score += 25
                if (isAutoLockEnabled) score += 15
                if (isEncryptionEnabled) score += 20
                if (clearOnFailedAttempts) score += 5
                if (hideInBackground) score += 3
                if (blockScreenshots) score += 2
                return score.coerceIn(0, 100)
            }

        val securityLevel: SecurityLevel
            get() = when {
                securityScore >= 90 -> SecurityLevel.EXCELLENT
                securityScore >= 70 -> SecurityLevel.GOOD
                securityScore >= 50 -> SecurityLevel.FAIR
                else -> SecurityLevel.WEAK
            }

        val pinStatus: StatusType
            get() = if (isPinEnabled) StatusType.SAFE else StatusType.OFF

        val biometricStatusType: StatusType
            get() = when {
                isBiometricEnabled -> StatusType.SAFE
                biometricAvailable -> StatusType.WEAK
                else -> StatusType.OFF
            }

        val encryptionStatusType: StatusType
            get() = when {
                isEncryptionEnabled -> StatusType.SAFE
                else -> StatusType.OFF
            }
    }

    enum class BiometricStatus {
        NOT_CONFIGURED,
        ACTIVE,
        UNAVAILABLE
    }

    enum class EncryptionStatus {
        NOT_CONFIGURED,
        ACTIVE,
        EXPIRED
    }

    enum class AutoLockDuration(val labelKey: String, val millis: Long) {
        IMMEDIATELY("auto_lock_immediately", 0),
        ONE_MINUTE("auto_lock_1min", 60_000L),
        FIVE_MINUTES("auto_lock_5min", 300_000L),
        FIFTEEN_MINUTES("auto_lock_15min", 900_000L),
        THIRTY_MINUTES("auto_lock_30min", 1_800_000L),
        NEVER("auto_lock_never", Long.MAX_VALUE)
    }

    enum class StatusType {
        SAFE,    // Green — configured and active
        WEAK,    // Orange — partially configured
        OFF      // Red — not configured
    }

    enum class SecurityLevel(val labelKey: String, val color: String) {
        EXCELLENT("security_excellent", "green"),
        GOOD("security_good", "blue"),
        FAIR("security_fair", "orange"),
        WEAK("security_weak", "red")
    }

    private val _state = MutableStateFlow(SecurityState())
    val state: StateFlow<SecurityState> = _state.asStateFlow()


    // ═══════════════════════════════════════════════════════════════
    // PIN LOCK ACTIONS
    // ═══════════════════════════════════════════════════════════════

    fun togglePinLock(enabled: Boolean) {
        if (!enabled) {
            // Removing PIN — reset
            _state.update {
                it.copy(
                    isPinEnabled = false,
                    pinLength = 0,
                    pinDots = List(4) { false }
                )
            }
        } else {
            _state.update { it.copy(isPinEnabled = true) }
        }
    }

    fun setPin(pin: String) {
        val dots = List(4) { index -> index < pin.length }
        _state.update {
            it.copy(
                pinLength = pin.length,
                pinDots = dots,
                isPinEnabled = pin.isNotEmpty()
            )
        }
    }

    fun changePin() {
        // Reset PIN state for re-entry
        _state.update {
            it.copy(
                pinLength = 0,
                pinDots = List(4) { false }
            )
        }
    }

    fun removePin() {
        _state.update {
            it.copy(
                isPinEnabled = false,
                pinLength = 0,
                pinDots = List(4) { false }
            )
        }
    }


    // ═══════════════════════════════════════════════════════════════
    // BIOMETRIC ACTIONS
    // ═══════════════════════════════════════════════════════════════

    fun toggleBiometric(enabled: Boolean) {
        _state.update {
            it.copy(
                isBiometricEnabled = enabled,
                biometricStatus = if (enabled) BiometricStatus.ACTIVE
                else BiometricStatus.NOT_CONFIGURED,
                savedFingerprints = if (enabled) it.savedFingerprints.coerceAtLeast(1) else 0
            )
        }
    }

    fun testBiometric() {
        // In real app: trigger BiometricPrompt
        // For now, just update state
        _state.update {
            it.copy(
                biometricStatus = BiometricStatus.ACTIVE,
                savedFingerprints = it.savedFingerprints.coerceAtLeast(1)
            )
        }
    }


    // ═══════════════════════════════════════════════════════════════
    // AUTO-LOCK ACTIONS
    // ═══════════════════════════════════════════════════════════════

    fun toggleAutoLock(enabled: Boolean) {
        _state.update { it.copy(isAutoLockEnabled = enabled) }
    }

    fun setAutoLockDuration(duration: AutoLockDuration) {
        _state.update {
            it.copy(
                autoLockDuration = duration,
                isAutoLockEnabled = duration != AutoLockDuration.NEVER
            )
        }
    }


    // ═══════════════════════════════════════════════════════════════
    // ENCRYPTION ACTIONS
    // ═══════════════════════════════════════════════════════════════

    fun toggleEncryption(enabled: Boolean) {
        _state.update {
            it.copy(
                isEncryptionEnabled = enabled,
                encryptionStatus = if (enabled) EncryptionStatus.ACTIVE
                else EncryptionStatus.NOT_CONFIGURED
            )
        }
    }

    fun changeEncryptionPassword() {
        // In real app: show password change dialog
    }


    // ═══════════════════════════════════════════════════════════════
    // SESSION SECURITY ACTIONS
    // ═══════════════════════════════════════════════════════════════

    fun toggleClearOnFailed(enabled: Boolean) {
        _state.update { it.copy(clearOnFailedAttempts = enabled) }
    }

    fun toggleHideInBackground(enabled: Boolean) {
        _state.update { it.copy(hideInBackground = enabled) }
    }

    fun toggleBlockScreenshots(enabled: Boolean) {
        _state.update { it.copy(blockScreenshots = enabled) }
    }


    // ═══════════════════════════════════════════════════════════════
    // DANGER ZONE ACTIONS
    // ═══════════════════════════════════════════════════════════════

    fun removeAllSecurity() {
        _state.update {
            SecurityState(
                language = it.language
            )
        }
    }

    fun resetAppData() {
        // In real app: show confirmation, then clear database
        removeAllSecurity()
    }


    // ═══════════════════════════════════════════════════════════════
    // LANGUAGE
    // ═══════════════════════════════════════════════════════════════

    fun setLanguage(lang: String) {
        _state.update { it.copy(language = lang) }
    }
}
