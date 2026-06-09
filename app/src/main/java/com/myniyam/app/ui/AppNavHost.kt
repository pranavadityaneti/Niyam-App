package com.myniyam.app.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.myniyam.app.R
import com.myniyam.app.permissions.PermissionChecker
import com.myniyam.app.ui.screens.OemAutostartScreen
import com.myniyam.app.ui.screens.PermissionScreen
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

        composable(NiyamRoutes.PERMISSION_USAGE) {
            val ctx = LocalContext.current
            PermissionScreen(
                titleResId = R.string.perm_usage_title,
                bodyResId = R.string.perm_usage_body,
                isGranted = { PermissionChecker.hasUsageStatsAccess(ctx) },
                launchSettings = { PermissionChecker.openUsageAccessSettings(ctx) },
                onGranted = { navController.navigate(NiyamRoutes.PERMISSION_OVERLAY) }
            )
        }

        composable(NiyamRoutes.PERMISSION_OVERLAY) {
            val ctx = LocalContext.current
            PermissionScreen(
                titleResId = R.string.perm_overlay_title,
                bodyResId = R.string.perm_overlay_body,
                isGranted = { PermissionChecker.hasOverlayPermission(ctx) },
                launchSettings = { PermissionChecker.openOverlayPermissionSettings(ctx) },
                onGranted = { navController.navigate(NiyamRoutes.PERMISSION_ACCESSIBILITY) }
            )
        }

        composable(NiyamRoutes.PERMISSION_ACCESSIBILITY) {
            val ctx = LocalContext.current
            PermissionScreen(
                titleResId = R.string.perm_accessibility_title,
                bodyResId = R.string.perm_accessibility_body,
                isGranted = { PermissionChecker.isAccessibilityServiceEnabled(ctx) },
                launchSettings = { PermissionChecker.openAccessibilitySettings(ctx) },
                onGranted = { navController.navigate(NiyamRoutes.PERMISSION_BATTERY) }
            )
        }

        composable(NiyamRoutes.PERMISSION_BATTERY) {
            val ctx = LocalContext.current
            PermissionScreen(
                titleResId = R.string.perm_battery_title,
                bodyResId = R.string.perm_battery_body,
                isGranted = { PermissionChecker.isIgnoringBatteryOptimizations(ctx) },
                launchSettings = { PermissionChecker.openIgnoreBatteryOptimizationSettings(ctx) },
                onGranted = { navController.navigate(NiyamRoutes.PERMISSION_OEM) }
            )
        }

        composable(NiyamRoutes.PERMISSION_OEM) {
            OemAutostartScreen(onDone = { navController.navigate(NiyamRoutes.HOME) })
        }
        composable(NiyamRoutes.HOME) { Text("home (Task 13)") }
    }
}
