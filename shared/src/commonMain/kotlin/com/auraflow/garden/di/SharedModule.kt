package com.auraflow.garden.di

import com.auraflow.garden.data.local.AuraFlowDatabase
import com.auraflow.garden.data.local.DatabaseMigrations
import com.auraflow.garden.data.local.createDatabaseBuilder
import com.auraflow.garden.data.network.ApiService
import com.auraflow.garden.data.repository.LevelRepository
import com.auraflow.garden.data.repository.PlayerRepository
import com.auraflow.garden.data.settings.AppSettings
import com.auraflow.garden.game.DifficultyAnalyzer
import com.auraflow.garden.game.StageManager
import com.auraflow.garden.game.engagement.AchievementSystem
import com.auraflow.garden.game.engagement.StreakManager
import com.auraflow.garden.ui.screens.game.GameViewModel
import com.auraflow.garden.ui.screens.game.luma.LumaViewModel
import com.auraflow.garden.ui.screens.home.HomeViewModel
import com.auraflow.garden.ui.screens.settings.SettingsViewModel
import com.auraflow.garden.ui.screens.zen.ZenViewModel
import com.auraflow.garden.ui.tutorial.TutorialViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val sharedModule = module {
    single<AuraFlowDatabase> {
        createDatabaseBuilder()
            .addMigrations(DatabaseMigrations.MIGRATION_1_2)
            .build()
    }
    single { LevelRepository() }
    single { PlayerRepository(get()) }
    single { AppSettings(get()) }
    single { StageManager(get(), get()) }
    single { DifficultyAnalyzer() }
    single { StreakManager(get()) }
    single { AchievementSystem(get()) }
    single { ApiService() }

    viewModel { GameViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { LumaViewModel() }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { TutorialViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { ZenViewModel(get()) }
}

fun initKoin(platformModules: List<Module> = emptyList()): KoinApplication {
    return startKoin {
        modules(sharedModule + platformModules)
    }
}
