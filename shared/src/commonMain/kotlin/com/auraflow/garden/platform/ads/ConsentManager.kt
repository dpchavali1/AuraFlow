package com.auraflow.garden.platform.ads

import kotlinx.coroutines.flow.StateFlow

/**
 * Cross-platform consent management.
 * Android: Google UMP SDK (UserMessagingPlatform)
 * iOS: Google UMP SDK + ATT (App Tracking Transparency)
 *
 * CRITICAL: Initialize this BEFORE Firebase, BEFORE MobileAds.
 * Never track or collect data before consent is obtained.
 */
expect class ConsentManager {
    val consentStatus: StateFlow<ConsentStatus>

    suspend fun requestConsentInfo()
    suspend fun showConsentFormIfRequired()
    fun canShowAds(): Boolean
}
