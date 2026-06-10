package com.myniyam.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.myniyam.app.R
import com.myniyam.app.onboarding.AppsScreen
import com.myniyam.app.onboarding.IntentionScreen
import com.myniyam.app.onboarding.LanguageScreen
import com.myniyam.app.onboarding.MantraPickerScreen
import com.myniyam.app.onboarding.OnboardingViewModel
import com.myniyam.app.permissions.PermissionChecker
import com.myniyam.app.ui.screens.HomeScreen
import com.myniyam.app.ui.screens.OemAutostartScreen
import com.myniyam.app.ui.screens.PermissionScreen
import com.myniyam.app.ui.screens.WelcomeScreen

object NiyamRoutes {
    const val WELCOME = "welcome"
    const val ONB_INTENTION = "onboarding_intention"
    const val ONB_MANTRA = "onboarding_mantra"
    const val ONB_LANGUAGE = "onboarding_language"
    const val ONB_APPS = "onboarding_apps"
    const val PERMISSION_USAGE = "permission_usage_stats"
    const val PERMISSION_OVERLAY = "permission_overlay"
    const val PERMISSION_ACCESSIBILITY = "permission_accessibility"
    const val PERMISSION_BATTERY = "permission_battery"
    const val PERMISSION_OEM = "permission_oem_autostart"
    const val HOME = "home"
}

@Composable
fun AppNavHost(
    startDestination: String,
    navController: NavHostController = rememberNavController()
) {
    val onboardingVm: OnboardingViewModel = viewModel()

    NavHost(navController = navController, startDestination = startDestination) {

        composable(NiyamRoutes.WELCOME) {
            WelcomeScreen(onGetStarted = { navController.navigate(NiyamRoutes.ONB_INTENTION) })
        }

        composable(NiyamRoutes.ONB_INTENTION) {
            IntentionScreen(onboardingVm) { navController.navigate(NiyamRoutes.ONB_MANTRA) }
        }
        composable(NiyamRoutes.ONB_MANTRA) {
            MantraPickerScreen(onboardingVm) { navController.navigate(NiyamRoutes.ONB_LANGUAGE) }
        }
        composable(NiyamRoutes.ONB_LANGUAGE) {
            LanguageScreen(onboardingVm) { navController.navigate(NiyamRoutes.ONB_APPS) }
        }
        composable(NiyamRoutes.ONB_APPS) {
            AppsScreen(onboardingVm) { navController.navigate(NiyamRoutes.PERMISSION_USAGE) }
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
            val ctx = LocalContext.current
            OemAutostartScreen(onDone = {
                onboardingVm.persistOnboardingComplete(ctx)
                navController.navigate(NiyamRoutes.HOME) {
                    popUpTo(NiyamRoutes.WELCOME) { inclusive = true }
                }
            })
        }

        composable(NiyamRoutes.HOME) {
            HomeScreen(onFixProtection = { navController.navigate(NiyamRoutes.PERMISSION_USAGE) })
        }
    }
}
