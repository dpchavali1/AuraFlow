package com.auraflow.garden.util

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.auraflow.garden.ui.navigation.NavGraph
import com.auraflow.garden.ui.theme.AuraFlowTheme

@Composable
fun AuraFlowApp() {
    AuraFlowTheme {
        Surface {
            val navController = rememberNavController()
            NavGraph(navController = navController)
        }
    }
}
