package com.auraflow.garden.platform.ads

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * iOS UMP + ATT consent implementation.
 *
 * CRITICAL ordering:
 * 1. UMP consent form (GDPR/CCPA) — required even for non-EU
 * 2. ATT prompt (only after UMP — Apple guideline)
 * 3. Firebase.configure() (only after consent)
 * 4. GADMobileAds.initialize()
 *
 * Info.plist requirements:
 * - NSUserTrackingUsageDescription
 * - FIREBASE_ANALYTICS_COLLECTION_ENABLED = NO (enable programmatically after consent)
 */
actual class ConsentManager {

    private val _consentStatus = MutableStateFlow(ConsentStatus.UNKNOWN)
    actual val consentStatus: StateFlow<ConsentStatus> = _consentStatus.asStateFlow()

    actual suspend fun requestConsentInfo() {
        // TODO Phase 15: UMPConsentInformation.sharedInstance.requestConsentInfoUpdateWithParameters(...)
        _consentStatus.value = ConsentStatus.NOT_REQUIRED
    }

    actual suspend fun showConsentFormIfRequired() {
        // TODO Phase 15: UMPConsentForm.loadAndPresentIfRequiredFromViewController(...)
        // After UMP: request ATT via ATTrackingManager.requestTrackingAuthorization
    }

    actual fun canShowAds(): Boolean {
        return _consentStatus.value == ConsentStatus.OBTAINED ||
            _consentStatus.value == ConsentStatus.NOT_REQUIRED
    }
}
