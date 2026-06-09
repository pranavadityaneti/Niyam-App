package com.myniyam.app.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.myniyam.app.ui.screens.WelcomeScreen

object NiyamRoutes {
    const val WELCOME = "welcome"
    const val PERMISSION_USAGE = "permission_usage_stats"
    const val PERMISSION_OVERLAY = "permission_overlay"
    const val PERMISSION_ACCESSIBILITY = "permission_accessibility"
    const val PERMISSION_BATTERY = "permission_battery"
    const val PERMISSION_OEM = "permission_oem_autostart"
    const val HOME = "home"
}

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = NiyamRoutes.WELCOME) {
        composable(NiyamRoutes.WELCOME) {
            WelcomeScreen(onGetStarted = { navController.navigate(NiyamRoutes.PERMISSION_USAGE) })
        }
        composable(NiyamRoutes.PERMISSION_USAGE) { Text("permission usage (Task 11)") }
        composable(NiyamRoutes.PERMISSION_OVERLAY) { Text("permission overlay (Task 11)") }
        composable(NiyamRoutes.PERMISSION_ACCESSIBILITY) { Text("permission accessibility (Task 11)") }
        composable(NiyamRoutes.PERMISSION_BATTERY) { Text("permission battery (Task 11)") }
        composable(NiyamRoutes.PERMISSION_OEM) { Text("permission oem (Task 12)") }
        composable(NiyamRoutes.HOME) { Text("home (Task 13)") }
    }
}
