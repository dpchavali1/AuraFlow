package com.auraflow.garden.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.auraflow.garden.ui.screens.game.GameScreen
import com.auraflow.garden.ui.screens.home.HomeScreen
import com.auraflow.garden.ui.screens.settings.SettingsScreen
import com.auraflow.garden.ui.screens.store.StoreScreen
import com.auraflow.garden.ui.screens.zen.ZenScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
    ) {
        composable<HomeRoute> {
            HomeScreen(
                onPlayStage = { stageId ->
                    navController.navigate(GameRoute(stageId))
                },
                onOpenZen = { navController.navigate(ZenRoute) },
                onOpenStore = { navController.navigate(StoreRoute) },
                onOpenSettings = { navController.navigate(SettingsRoute) },
            )
        }
        composable<GameRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<GameRoute>()
            GameScreen(
                stageId = route.stageId,
                onBack = { navController.popBackStack() },
                onNextLevel = { nextStageId ->
                    navController.navigate(GameRoute(nextStageId)) {
                        popUpTo(GameRoute(route.stageId)) { inclusive = true }
                    }
                },
            )
        }
        composable<ZenRoute> {
            ZenScreen(onBack = { navController.popBackStack() })
        }
        composable<StoreRoute> {
            StoreScreen(onBack = { navController.popBackStack() })
        }
        composable<SettingsRoute> {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
