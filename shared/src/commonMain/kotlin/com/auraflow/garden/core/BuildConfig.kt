package com.auraflow.garden.core

/**
 * Build-time configuration.
 * Debug values here. Production overrides are set via platform-specific build configs.
 * Android: BuildConfig.DEBUG / BuildConfig.APPLICATION_ID
 * iOS: scheme environment variables or Info.plist
 */
object BuildConfig {
    const val APPLICATION_ID = "com.auraflow.garden"
    const val VERSION_NAME = "1.0.0"
    const val VERSION_CODE = 1
    const val API_BASE_URL = "https://api.auraflow.garden"
    const val DEBUG = false  // Set true in debug builds via platform config
}
