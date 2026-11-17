package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.PreferencesRepository
import com.example.domain.model.DeenFontConfig
import com.example.domain.model.FontScalePreset
import com.example.domain.model.LatinFontChoice
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FontSettingsUiState(
    val fontConfig: DeenFontConfig = DeenFontConfig()
)

class FontSettingsViewModel(
    private val prefsRepo: PreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<FontSettingsUiState> = prefsRepo.userPreferences
        .map { FontSettingsUiState(fontConfig = it.fontConfig) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FontSettingsUiState()
        )

    fun setPreset(preset: FontScalePreset) {
        val current = uiState.value.fontConfig
        viewModelScope.launch {
            prefsRepo.setFontConfig(current.copy(appScalePreset = preset))
        }
    }

    fun setArabicScale(scale: Float) {
        val current = uiState.value.fontConfig
        viewModelScope.launch {
            prefsRepo.setFontConfig(current.copy(arabicFontScale = scale.coerceIn(0.85f, 1.50f)))
        }
    }

    fun setBengaliScale(scale: Float) {
        val current = uiState.value.fontConfig
        viewModelScope.launch {
            prefsRepo.setFontConfig(current.copy(bengaliFontScale = scale.coerceIn(0.85f, 1.45f)))
        }
    }

    fun setEnglishScale(scale: Float) {
        val current = uiState.value.fontConfig
        viewModelScope.launch {
            prefsRepo.setFontConfig(current.copy(englishFontScale = scale.coerceIn(0.85f, 1.45f)))
        }
    }

    fun setLatinFontChoice(choice: LatinFontChoice) {
        val current = uiState.value.fontConfig
        viewModelScope.launch {
            prefsRepo.setFontConfig(current.copy(latinFontChoice = choice))
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            prefsRepo.resetFontConfig()
        }
    }
}
