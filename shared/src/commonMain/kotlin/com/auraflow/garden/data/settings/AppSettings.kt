package com.auraflow.garden.data.settings

import com.russhwolf.settings.Settings

/**
 * Key-value preferences storage using Multiplatform Settings.
 * All keys are defined as constants to prevent typos.
 */
class AppSettings(private val settings: Settings) {

    companion object {
        const val KEY_HAPTICS_ENABLED = "haptics_enabled"
        const val KEY_SFX_VOLUME = "sfx_volume"
        const val KEY_MUSIC_VOLUME = "music_volume"
        const val KEY_COLOR_BLIND_MODE = "color_blind_mode"
        const val KEY_CONNECT_MODE = "connect_mode"
    }

    var hapticsEnabled: Boolean
        get() = settings.getBoolean(KEY_HAPTICS_ENABLED, defaultValue = true)
        set(value) = settings.putBoolean(KEY_HAPTICS_ENABLED, value)

    var sfxVolume: Float
        get() = settings.getFloat(KEY_SFX_VOLUME, defaultValue = 1.0f)
        set(value) = settings.putFloat(KEY_SFX_VOLUME, value)

    var musicVolume: Float
        get() = settings.getFloat(KEY_MUSIC_VOLUME, defaultValue = 0.7f)
        set(value) = settings.putFloat(KEY_MUSIC_VOLUME, value)

    var colorBlindModeEnabled: Boolean
        get() = settings.getBoolean(KEY_COLOR_BLIND_MODE, defaultValue = false)
        set(value) = settings.putBoolean(KEY_COLOR_BLIND_MODE, value)

    var connectModeEnabled: Boolean
        get() = settings.getBoolean(KEY_CONNECT_MODE, defaultValue = false)
        set(value) = settings.putBoolean(KEY_CONNECT_MODE, value)
}
