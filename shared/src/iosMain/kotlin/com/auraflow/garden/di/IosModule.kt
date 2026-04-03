package com.auraflow.garden.di

import com.auraflow.garden.platform.PlatformContext
import com.auraflow.garden.platform.audio.AudioEngine
import com.auraflow.garden.platform.ads.AdManager
import com.auraflow.garden.platform.ads.ConsentManager
import com.auraflow.garden.platform.billing.BillingManager
import com.auraflow.garden.platform.billing.EntitlementManager
import com.auraflow.garden.platform.haptics.HapticEngine
import com.auraflow.garden.ui.screens.store.StoreViewModel
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

val iosModule = module {
    single { PlatformContext() }
    single { HapticEngine() }
    single { AudioEngine() }
    single<Settings> {
        NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
    }
    single { BillingManager() }
    single { EntitlementManager(get(), get(), get()) }
    single { ConsentManager() }
    single { AdManager() }
    viewModel { StoreViewModel(get(), get()) }
}
