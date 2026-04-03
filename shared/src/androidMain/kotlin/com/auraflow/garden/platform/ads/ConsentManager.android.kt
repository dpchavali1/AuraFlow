package com.auraflow.garden.platform.ads

import com.auraflow.garden.platform.PlatformContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android UMP (User Messaging Platform) consent implementation.
 * Requires: implementation(libs.ump) in build.gradle.kts.
 * Firebase analytics disabled until consent obtained (via AndroidManifest meta-data).
 */
actual class ConsentManager(private val platformContext: PlatformContext) {

    private val _consentStatus = MutableStateFlow(ConsentStatus.UNKNOWN)
    actual val consentStatus: StateFlow<ConsentStatus> = _consentStatus.asStateFlow()

    actual suspend fun requestConsentInfo() {
        // TODO Phase 15: ConsentInformation.getInstance().requestConsentInfoUpdate(...)
        _consentStatus.value = ConsentStatus.NOT_REQUIRED
    }

    actual suspend fun showConsentFormIfRequired() {
        // TODO Phase 15: UserMessagingPlatform.loadAndShowConsentFormIfRequired(...)
    }

    actual fun canShowAds(): Boolean {
        return _consentStatus.value == ConsentStatus.OBTAINED ||
            _consentStatus.value == ConsentStatus.NOT_REQUIRED
    }
}
