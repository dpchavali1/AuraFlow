package com.auraflow.garden.platform.ads

import kotlinx.coroutines.flow.StateFlow

/**
 * Cross-platform AdMob Rewarded Video wrapper.
 * ONLY used in Daily Challenge, ONLY after failure.
 * Android actual: Google Mobile Ads SDK + UMP consent
 * iOS actual: Google Mobile Ads iOS SDK + ATT + UMP
 *
 * Consent sequencing (iOS):
 * 1. Show UMP form
 * 2. After UMP: request ATT
 * 3. After ATT: Firebase.configure()
 * 4. After Firebase: initialize MobileAds + load ad
 */
expect class AdManager {
    val adState: StateFlow<AdState>

    suspend fun initialize(consentGranted: Boolean)
    suspend fun preloadRewardedAd()
    suspend fun showRewardedAd(): AdRewardResult
}
