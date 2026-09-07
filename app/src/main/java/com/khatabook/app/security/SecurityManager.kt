package com.khatabook.app.security

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized security manager for PIN lock, biometric auth, and auto-lock.
 *
 * - PIN is stored as a plain string (4-digit numeric).
 * - Biometric failures are tracked; after 3 failures, biometric is blocked until PIN is entered.
 * - Auto-lock records background time and decides whether to lock on resume.
 */
@Singleton
class SecurityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("khata_security", Context.MODE_PRIVATE)
    }

    companion object {
        private const val KEY_PIN = "app_pin"
        private const val KEY_PIN_ENABLED = "pin_lock_enabled"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_AUTO_LOCK_MINUTES = "auto_lock_minutes"
        private const val KEY_LAST_BACKGROUND = "last_background_time"
        private const val KEY_BIOMETRIC_FAILURES = "biometric_failures"
        private const val KEY_BIOMETRIC_BLOCKED = "biometric_blocked"
        private const val KEY_IS_LOCKED = "is_locked"
        private const val MAX_BIOMETRIC_FAILURES = 3
    }

    // ═══════════════════ GETTERS ═══════════════════

    val isPinEnabled: Boolean get() = prefs.getBoolean(KEY_PIN_ENABLED, false)
    val isBiometricEnabled: Boolean get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    val autoLockMinutes: Int get() = prefs.getInt(KEY_AUTO_LOCK_MINUTES, -1)
    val isBiometricBlocked: Boolean get() = prefs.getBoolean(KEY_BIOMETRIC_BLOCKED, false)
    val biometricFailureCount: Int get() = prefs.getInt(KEY_BIOMETRIC_FAILURES, 0)
    val storedPin: String? get() = prefs.getString(KEY_PIN, null)

    val isLocked: Boolean
        get() {
            if (!isPinEnabled) return false
            if (autoLockMinutes == -1) return false // never auto-lock

            val lastBg = prefs.getLong(KEY_LAST_BACKGROUND, 0)
            if (lastBg == 0L) return true // never went to background, lock on start

            val elapsed = (System.currentTimeMillis() - lastBg) / 60_000
            return elapsed >= autoLockMinutes
        }

    // ═══════════════════ PIN ═══════════════════

    fun setPin(pin: String) {
        prefs.edit()
            .putString(KEY_PIN, pin)
            .putBoolean(KEY_PIN_ENABLED, true)
            .putBoolean(KEY_IS_LOCKED, false)
            .apply()
    }

    fun removePin() {
        prefs.edit()
            .remove(KEY_PIN)
            .putBoolean(KEY_PIN_ENABLED, false)
            .putBoolean(KEY_IS_LOCKED, false)
            .putInt(KEY_AUTO_LOCK_MINUTES, -1)
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
            .putInt(KEY_BIOMETRIC_FAILURES, 0)
            .putBoolean(KEY_BIOMETRIC_BLOCKED, false)
            .apply()
    }

    fun verifyPin(pin: String): Boolean {
        return storedPin == pin
    }

    // ═══════════════════ BIOMETRIC ═══════════════════

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit()
            .putBoolean(KEY_BIOMETRIC_ENABLED, enabled)
            .apply()
        if (enabled) {
            resetBiometricFailures()
        }
    }

    fun recordBiometricFailure(): Boolean {
        val count = biometricFailureCount + 1
        val blocked = count >= MAX_BIOMETRIC_FAILURES
        prefs.edit()
            .putInt(KEY_BIOMETRIC_FAILURES, count)
            .putBoolean(KEY_BIOMETRIC_BLOCKED, blocked)
            .apply()
        return blocked
    }

    fun resetBiometricFailures() {
        prefs.edit()
            .putInt(KEY_BIOMETRIC_FAILURES, 0)
            .putBoolean(KEY_BIOMETRIC_BLOCKED, false)
            .apply()
    }

    // ═══════════════════ AUTO-LOCK ═══════════════════

    fun setAutoLockMinutes(minutes: Int) {
        prefs.edit()
            .putInt(KEY_AUTO_LOCK_MINUTES, minutes)
            .apply()
    }

    /** Called when app goes to background */
    fun onAppBackground() {
        prefs.edit()
            .putLong(KEY_LAST_BACKGROUND, System.currentTimeMillis())
            .apply()
    }

    /** Called when app resumes — returns true if should show lock screen */
    fun onAppForeground(): Boolean {
        if (!isPinEnabled) return false
        if (autoLockMinutes == -1) return false

        if (autoLockMinutes == 0) return true // immediately lock

        val lastBg = prefs.getLong(KEY_LAST_BACKGROUND, 0)
        if (lastBg == 0L) return true

        val elapsed = (System.currentTimeMillis() - lastBg) / 60_000
        return elapsed >= autoLockMinutes
    }

    /** Called after successful unlock */
    fun unlock() {
        prefs.edit()
            .putBoolean(KEY_IS_LOCKED, false)
            .putLong(KEY_LAST_BACKGROUND, 0)
            .apply()
        resetBiometricFailures()
    }
}
