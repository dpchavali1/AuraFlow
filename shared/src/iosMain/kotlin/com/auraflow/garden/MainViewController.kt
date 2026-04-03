package com.auraflow.garden

import androidx.compose.ui.window.ComposeUIViewController
import com.auraflow.garden.di.iosModule
import com.auraflow.garden.di.initKoin
import com.auraflow.garden.util.AuraFlowApp

fun MainViewController() = ComposeUIViewController {
    AuraFlowApp()
}

fun initKoinIos() {
    initKoin(platformModules = listOf(iosModule))
}
