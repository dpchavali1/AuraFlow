package com.auraflow.garden.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data class GameRoute(val stageId: Int)

@Serializable
data object ZenRoute

@Serializable
data object StoreRoute

@Serializable
data object SettingsRoute
