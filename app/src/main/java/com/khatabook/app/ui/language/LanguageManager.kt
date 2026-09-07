package com.khatabook.app.ui.language

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.khatabook.app.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ═══════════════════════════════════════════════════════════════════
 * LANGUAGE MANAGER — Manages app-wide language state
 * ═══════════════════════════════════════════════════════════════════
 *
 * Persists language selection to SharedPreferences.
 * Provides reactive language code for KhataTheme.
 */
@HiltViewModel
class LanguageManager @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentLanguage = MutableStateFlow(loadLanguage())
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    val currentLanguageCode: String get() = _currentLanguage.value

    fun setLanguage(code: String) {
        _currentLanguage.value = code
        prefs.edit().putString(Constants.KEY_LANGUAGE, code).apply()
        prefs.edit().putBoolean(Constants.KEY_LANGUAGE_SELECTED, true).apply()
    }

    fun getLanguageDisplay(): String = when (_currentLanguage.value) {
        "en" -> "English"
        "ur" -> "اردو"
        "ur-roman" -> "Roman Urdu"
        else -> "English"
    }

    private fun loadLanguage(): String {
        return prefs.getString(Constants.KEY_LANGUAGE, Constants.LANG_ENGLISH) ?: Constants.LANG_ENGLISH
    }
}
