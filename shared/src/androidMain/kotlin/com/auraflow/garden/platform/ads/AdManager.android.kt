package com.auraflow.garden.platform.ads

import com.auraflow.garden.platform.PlatformContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Android AdMob Rewarded Video implementation.
 * Requires: implementation(libs.admob) in build.gradle.kts (Phase 15).
 *
 * Test Ad Unit ID: ca-app-pub-3940256099942544/5224354917
 * Production: replace with real ad unit ID from AdMob console.
 *
 * AdMob SDK activation is guarded by ConsentManager.
 * Do NOT call MobileAds.initialize() until consent is obtained.
 */
actual class AdManager(private val platformContext: PlatformContext) {

    private val _adState = MutableStateFlow(AdState.NOT_LOADED)
    actual val adState: StateFlow<AdState> = _adState.asStateFlow()

    // Test ad unit ID — replace in production
    private val adUnitId = "ca-app-pub-3940256099942544/5224354917"

    actual suspend fun initialize(consentGranted: Boolean) {
        if (!consentGranted) {
            _adState.value = AdState.FAILED
            return
        }
        // TODO Phase 15: MobileAds.initialize(platformContext.context) { preloadRewardedAd() }
        _adState.value = AdState.NOT_LOADED
    }

    actual suspend fun preloadRewardedAd() {
        _adState.value = AdState.LOADING
        // TODO Phase 15: RewardedAd.load(platformContext.context, adUnitId, ...)
        // Stub — not loaded in this build
        _adState.value = AdState.FAILED
    }

    actual suspend fun showRewardedAd(): AdRewardResult {
        return AdRewardResult.Failed("AdMob not initialized in this build")
    }
}
