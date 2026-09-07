package com.khatabook.app.ui.language

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ═══════════════════════════════════════════════════════════════════
 * LANGUAGE SELECTION VIEW MODEL — Manages language selection state
 * ═══════════════════════════════════════════════════════════════════
 *
 * State flow:
 *   User taps language card → Preview updates → User taps Continue → Save & navigate
 */
class LanguageSelectionViewModel : ViewModel() {

    // ═══════════════════════════════════════════════════════════════
    // UI STATE
    // ═══════════════════════════════════════════════════════════════

    data class LanguageSelectionState(
        val selectedLanguageCode: String = "en",
        val isLanguageSelected: Boolean = false,
        val hasSavedLanguage: Boolean = false
    ) {
        val selectedLanguage: KhataLanguage
            get() = KhataLanguages.getByCode(selectedLanguageCode)

        val allLanguages: List<KhataLanguage>
            get() = KhataLanguages.allLanguages
    }

    private val _state = MutableStateFlow(LanguageSelectionState())
    val state: StateFlow<LanguageSelectionState> = _state.asStateFlow()


    // ═══════════════════════════════════════════════════════════════
    // ACTIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * User taps a language card.
     */
    fun selectLanguage(languageCode: String) {
        _state.update {
            it.copy(
                selectedLanguageCode = languageCode,
                isLanguageSelected = true
            )
        }
    }

    /**
     * User taps Continue button.
     * In real app: save to DataStore, update app locale, navigate to home.
     */
    fun continueWithSelection(): String {
        return _state.value.selectedLanguageCode
    }

    /**
     * Load previously saved language.
     * Called on screen entry.
     */
    fun loadSavedLanguage(languageCode: String = "en") {
        _state.update {
            LanguageSelectionState(
                selectedLanguageCode = languageCode,
                isLanguageSelected = languageCode.isNotEmpty(),
                hasSavedLanguage = languageCode.isNotEmpty()
            )
        }
    }

    /**
     * Check if this is first-time language selection.
     */
    fun isFirstTime(): Boolean {
        return !_state.value.hasSavedLanguage
    }
}
