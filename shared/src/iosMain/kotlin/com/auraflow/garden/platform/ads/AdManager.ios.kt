package com.auraflow.garden.platform.ads

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * iOS AdMob Rewarded Video implementation.
 * Requires: pod 'Google-Mobile-Ads-SDK' in Podfile (Phase 15).
 * Requires: NSUserTrackingUsageDescription in Info.plist.
 * Requires: GADApplicationIdentifier in Info.plist.
 *
 * Consent sequencing:
 * 1. ConsentManager.showConsentFormIfRequired() (UMP)
 * 2. ATTrackingManager.requestTrackingAuthorization (ATT)
 * 3. Firebase.configure()
 * 4. GADMobileAds.sharedInstance().start()
 * 5. preloadRewardedAd()
 */
actual class AdManager {

    private val _adState = MutableStateFlow(AdState.NOT_LOADED)
    actual val adState: StateFlow<AdState> = _adState.asStateFlow()

    actual suspend fun initialize(consentGranted: Boolean) {
        if (!consentGranted) {
            _adState.value = AdState.FAILED
            return
        }
        // TODO Phase 15: GADMobileAds.sharedInstance().startWithCompletionHandler(...)
        _adState.value = AdState.NOT_LOADED
    }

    actual suspend fun preloadRewardedAd() {
        _adState.value = AdState.LOADING
        // TODO Phase 15: GADRewardedAd.loadWithAdUnitID(...)
        _adState.value = AdState.FAILED
    }

    actual suspend fun showRewardedAd(): AdRewardResult {
        return AdRewardResult.Failed("AdMob not initialized in this build")
    }
}
