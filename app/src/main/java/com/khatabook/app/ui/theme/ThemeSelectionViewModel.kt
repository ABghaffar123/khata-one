package com.khatabook.app.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ThemeSelectionViewModel : ViewModel() {

    data class ThemeSelectionState(
        val selectedPresetId: String = "fire_orange",
        val displayMode: KhataGradientPresets.DisplayMode = KhataGradientPresets.DisplayMode.SYSTEM,
        val hasUnsavedChanges: Boolean = false,
        val isApplied: Boolean = false,
        val language: String = "en"
    ) {
        val selectedPreset: KhataGradientPreset
            get() = KhataGradientPresets.getById(selectedPresetId)

        val previewTheme: KhataGradientPreset
            get() = selectedPreset
    }

    private val _state = MutableStateFlow(ThemeSelectionState())
    val state: StateFlow<ThemeSelectionState> = _state.asStateFlow()

    fun selectPreset(presetId: String) {
        _state.update { currentState ->
            currentState.copy(
                selectedPresetId = presetId,
                hasUnsavedChanges = currentState.selectedPresetId != presetId,
                isApplied = false
            )
        }
        markUnsaved()
    }

    fun selectDisplayMode(mode: KhataGradientPresets.DisplayMode) {
        _state.update { it.copy(displayMode = mode, isApplied = false) }
        markUnsaved()
    }

    fun applyTheme() {
        viewModelScope.launch {
            _state.update { it.copy(hasUnsavedChanges = false, isApplied = true) }
        }
    }

    fun resetToDefault() {
        _state.update {
            ThemeSelectionState(
                selectedPresetId = "fire_orange",
                displayMode = KhataGradientPresets.DisplayMode.SYSTEM,
                hasUnsavedChanges = true,
                isApplied = false,
                language = it.language
            )
        }
    }

    fun setLanguage(lang: String) {
        _state.update { it.copy(language = lang) }
    }

    fun loadSavedTheme(
        presetId: String = "fire_orange",
        displayMode: KhataGradientPresets.DisplayMode = KhataGradientPresets.DisplayMode.SYSTEM,
        lang: String = "en"
    ) {
        _state.update {
            ThemeSelectionState(
                selectedPresetId = presetId,
                displayMode = displayMode,
                language = lang
            )
        }
    }

    private fun markUnsaved() {
        _state.update { it.copy(hasUnsavedChanges = true) }
    }
}
