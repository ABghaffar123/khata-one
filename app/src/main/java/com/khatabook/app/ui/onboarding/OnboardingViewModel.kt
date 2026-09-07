package com.khatabook.app.ui.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ═══════════════════════════════════════════════════════════════════
 * ONBOARDING VIEW MODEL — Manages onboarding flow state
 * ═══════════════════════════════════════════════════════════════════
 *
 * State flow:
 *   Page swipe → Update indicator → Continue → Next page → Finish → Navigate
 */
class OnboardingViewModel : ViewModel() {

    // ═══════════════════════════════════════════════════════════════
    // UI STATE
    // ═══════════════════════════════════════════════════════════════

    data class OnboardingState(
        val currentPage: Int = 0,
        val totalPages: Int = 4,
        val selectedLanguageCode: String = "en",
        val isLanguageSelected: Boolean = false,
        val isCompleted: Boolean = false,
        val isSkipped: Boolean = false
    ) {
        val currentScreen: OnboardingScreen
            get() = OnboardingScreens.allScreens.getOrElse(currentPage) {
                OnboardingScreens.Welcome
            }

        val isLastPage: Boolean
            get() = currentPage >= totalPages - 1

        val isFirstPage: Boolean
            get() = currentPage == 0

        val progress: Float
            get() = (currentPage + 1).toFloat() / totalPages

        val selectedLanguage: com.khatabook.app.ui.language.KhataLanguage
            get() = com.khatabook.app.ui.language.KhataLanguages.getByCode(selectedLanguageCode)
    }

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()


    // ═══════════════════════════════════════════════════════════════
    // ACTIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * User swipes to a new page.
     */
    fun onPageChanged(page: Int) {
        _state.update { it.copy(currentPage = page) }
    }

    /**
     * User taps Continue / Get Started.
     */
    fun nextPage() {
        _state.update { currentState ->
            if (currentState.isLastPage) {
                currentState.copy(isCompleted = true)
            } else {
                currentState.copy(currentPage = currentState.currentPage + 1)
            }
        }
    }

    /**
     * User taps Skip.
     */
    fun skip() {
        _state.update {
            it.copy(
                isSkipped = true,
                isCompleted = true
            )
        }
    }

    /**
     * User selects a language on screen 1.
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
     * Get the selected language code for persistence.
     */
    fun getSelectedLanguage(): String {
        return _state.value.selectedLanguageCode
    }

    /**
     * Check if onboarding is complete.
     */
    fun isComplete(): Boolean {
        return _state.value.isCompleted
    }
}
