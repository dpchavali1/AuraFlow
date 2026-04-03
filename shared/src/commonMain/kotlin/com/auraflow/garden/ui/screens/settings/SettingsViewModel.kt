package com.auraflow.garden.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.auraflow.garden.data.settings.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsUiState(
    val hapticsEnabled: Boolean = true,
    val sfxVolume: Float = 1.0f,
    val musicVolume: Float = 0.7f,
)

class SettingsViewModel(private val appSettings: AppSettings) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingsUiState(
            hapticsEnabled = appSettings.hapticsEnabled,
            sfxVolume = appSettings.sfxVolume,
            musicVolume = appSettings.musicVolume,
        )
    )
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    fun setHapticsEnabled(enabled: Boolean) {
        appSettings.hapticsEnabled = enabled
        _state.value = _state.value.copy(hapticsEnabled = enabled)
    }

    fun setSfxVolume(volume: Float) {
        appSettings.sfxVolume = volume
        _state.value = _state.value.copy(sfxVolume = volume)
    }

    fun setMusicVolume(volume: Float) {
        appSettings.musicVolume = volume
        _state.value = _state.value.copy(musicVolume = volume)
    }
}
