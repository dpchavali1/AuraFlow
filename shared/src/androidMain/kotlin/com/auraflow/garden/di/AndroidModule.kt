package com.auraflow.garden.di

import android.content.Context
import com.auraflow.garden.platform.PlatformContext
import com.auraflow.garden.platform.audio.AudioEngine
import com.auraflow.garden.platform.ads.AdManager
import com.auraflow.garden.platform.ads.ConsentManager
import com.auraflow.garden.platform.billing.BillingManager
import com.auraflow.garden.platform.billing.EntitlementManager
import com.auraflow.garden.platform.haptics.HapticEngine
import com.auraflow.garden.ui.screens.store.StoreViewModel
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun androidModule(context: Context) = module {
    single { PlatformContext(context) }
    single { HapticEngine(get()) }
    single { AudioEngine(get()) }
    single<Settings> {
        SharedPreferencesSettings(
            context.getSharedPreferences("auraflow_prefs", Context.MODE_PRIVATE)
        )
    }
    single { BillingManager(get()) }
    single { EntitlementManager(get(), get(), get()) }
    single { ConsentManager(get()) }
    single { AdManager(get()) }
    viewModel { StoreViewModel(get(), get()) }
}
