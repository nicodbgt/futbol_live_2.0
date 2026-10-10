package com.app.relatosfutbolerosnd_20.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.relatosfutbolerosnd_20.model.MatchConfig
import com.app.relatosfutbolerosnd_20.ui.screens.LicenseScreen
import com.app.relatosfutbolerosnd_20.ui.screens.LiveStreamScreen
import com.app.relatosfutbolerosnd_20.ui.screens.MatchSetupScreen
import com.app.relatosfutbolerosnd_20.ui.screens.SplashScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(onNavigate = { route ->
                navController.navigate(route) {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        
        composable("license") {
            LicenseScreen(
                onLicenseValidated = {
                    navController.navigate("setup") {
                        popUpTo("license") { inclusive = true }
                    }
                }
            )
        }

        composable("setup") {
            MatchSetupScreen(navController = navController)
        }

        composable("live") {
            val config = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<MatchConfig>("matchConfig")
                ?: MatchConfig()
            LiveStreamScreen(navController = navController, matchConfig = config)
        }
    }
}