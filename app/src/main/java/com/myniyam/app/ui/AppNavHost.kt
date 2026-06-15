package com.myniyam.app.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.myniyam.app.R
import com.myniyam.app.billing.PaywallScreen
import com.myniyam.app.library.FavouritesScreen
import com.myniyam.app.library.LibraryScreen
import com.myniyam.app.library.MantraDetailScreen
import com.myniyam.app.data.UserPrefs
import com.myniyam.app.onboarding.AppsScreen
import com.myniyam.app.onboarding.IntentionScreen
import com.myniyam.app.onboarding.LanguageScreen
import com.myniyam.app.onboarding.MantraPickerScreen
import com.myniyam.app.onboarding.OnboardingViewModel
import com.myniyam.app.permissions.PermissionChecker
import com.myniyam.app.settings.SettingsScreen
import com.myniyam.app.settings.LanguageSettingScreen
import com.myniyam.app.settings.BlockedAppsSettingScreen
import com.myniyam.app.settings.IntentionSettingScreen
import com.myniyam.app.ui.screens.CelebrationScreen
import com.myniyam.app.ui.screens.HomeScreen
import com.myniyam.app.ui.screens.NextSadhanaScreen
import com.myniyam.app.ui.screens.OemAutostartScreen
import com.myniyam.app.ui.screens.PermissionScreen
import com.myniyam.app.ui.screens.SignInScreen
import com.myniyam.app.ui.screens.WelcomeScreen
import kotlinx.coroutines.launch

object NiyamRoutes {
    const val WELCOME = "welcome"
    const val SIGN_IN = "sign_in"
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
    const val CELEBRATION = "celebration"
    const val NEXT_SADHANA = "next_sadhana"
    const val LIBRARY = "library"
    const val FAVOURITES = "favourites"
    const val MANTRA_DETAIL = "mantra_detail/{mantraId}"
    const val SETTINGS = "settings"
    const val SETTINGS_LANGUAGE = "settings_language"
    const val SETTINGS_APPS = "settings_apps"
    const val SETTINGS_INTENTION = "settings_intention"
    const val PAYWALL = "paywall"
}

@Composable
fun AppNavHost(
    startDestination: String,
    navController: NavHostController = rememberNavController()
) {
    val onboardingVm: OnboardingViewModel = viewModel()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val topLevel = setOf(NiyamRoutes.HOME, NiyamRoutes.LIBRARY, NiyamRoutes.FAVOURITES, NiyamRoutes.SETTINGS)

    Box(Modifier.fillMaxSize()) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(animationSpec = tween(220)) },
        exitTransition = { fadeOut(animationSpec = tween(220)) },
        popEnterTransition = { fadeIn(animationSpec = tween(220)) },
        popExitTransition = { fadeOut(animationSpec = tween(220)) }
    ) {

        composable(NiyamRoutes.WELCOME) {
            WelcomeScreen(onGetStarted = { navController.navigate(NiyamRoutes.SIGN_IN) })
        }

        composable(NiyamRoutes.SIGN_IN) {
            SignInScreen(onSignedIn = {
                navController.navigate(NiyamRoutes.ONB_INTENTION) {
                    popUpTo(NiyamRoutes.SIGN_IN) { inclusive = true }
                }
            })
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
                onGranted = { navController.navigate(NiyamRoutes.PERMISSION_OVERLAY) },
                stepIndex = 0
            )
        }

        composable(NiyamRoutes.PERMISSION_OVERLAY) {
            val ctx = LocalContext.current
            PermissionScreen(
                titleResId = R.string.perm_overlay_title,
                bodyResId = R.string.perm_overlay_body,
                isGranted = { PermissionChecker.hasOverlayPermission(ctx) },
                launchSettings = { PermissionChecker.openOverlayPermissionSettings(ctx) },
                onGranted = { navController.navigate(NiyamRoutes.PERMISSION_ACCESSIBILITY) },
                stepIndex = 1
            )
        }

        composable(NiyamRoutes.PERMISSION_ACCESSIBILITY) {
            val ctx = LocalContext.current
            val scope = rememberCoroutineScope()
            PermissionScreen(
                titleResId = R.string.perm_accessibility_title,
                bodyResId = R.string.perm_accessibility_body,
                isGranted = { PermissionChecker.isAccessibilityServiceEnabled(ctx) },
                launchSettings = { PermissionChecker.openAccessibilitySettings(ctx) },
                onGranted = { navController.navigate(NiyamRoutes.PERMISSION_BATTERY) },
                stepIndex = 2,
                // Play prominent-disclosure + affirmative consent (SP-16 §A)
                disclosureResId = R.string.perm_accessibility_disclosure,
                ctaResId = R.string.perm_accessibility_agree,
                onConsent = {
                    scope.launch {
                        UserPrefs.recordAccessibilityConsent(ctx, System.currentTimeMillis())
                    }
                }
            )
        }

        composable(NiyamRoutes.PERMISSION_BATTERY) {
            val ctx = LocalContext.current
            PermissionScreen(
                titleResId = R.string.perm_battery_title,
                bodyResId = R.string.perm_battery_body,
                isGranted = { PermissionChecker.isIgnoringBatteryOptimizations(ctx) },
                launchSettings = { PermissionChecker.openIgnoreBatteryOptimizationSettings(ctx) },
                onGranted = { navController.navigate(NiyamRoutes.PERMISSION_OEM) },
                stepIndex = 3
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
            LaunchedEffect(Unit) {
                if (UserPrefs.snapshot().pendingCelebration) {
                    navController.navigate(NiyamRoutes.CELEBRATION)
                }
            }
            HomeScreen(
                onFixProtection = { navController.navigate(NiyamRoutes.PERMISSION_USAGE) },
                onBrowseLibrary = { navController.navigate(NiyamRoutes.LIBRARY) },
                onOpenSettings = { navController.navigate(NiyamRoutes.SETTINGS) }
            )
        }

        composable(NiyamRoutes.CELEBRATION) {
            val ctx = LocalContext.current
            val scope = rememberCoroutineScope()
            CelebrationScreen(
                onChooseNext = { navController.navigate(NiyamRoutes.NEXT_SADHANA) },
                onKeepCurrent = {
                    scope.launch {
                        UserPrefs.setCurrentMantra(ctx, UserPrefs.snapshot().currentMantraId)
                        navController.popBackStack(NiyamRoutes.HOME, inclusive = false)
                    }
                }
            )
        }

        composable(NiyamRoutes.NEXT_SADHANA) {
            NextSadhanaScreen(onDone = { navController.popBackStack(NiyamRoutes.HOME, inclusive = false) })
        }

        composable(NiyamRoutes.LIBRARY) {
            LibraryScreen(onOpenDetail = { id -> navController.navigate("mantra_detail/$id") })
        }

        composable(NiyamRoutes.FAVOURITES) {
            FavouritesScreen(onOpenDetail = { id -> navController.navigate("mantra_detail/$id") })
        }

        composable(
            NiyamRoutes.MANTRA_DETAIL,
            arguments = listOf(navArgument("mantraId") { type = NavType.StringType })
        ) { backStackEntry ->
            MantraDetailScreen(
                mantraId = backStackEntry.arguments?.getString("mantraId") ?: "",
                onSwitched = { navController.popBackStack(NiyamRoutes.HOME, inclusive = false) },
                onMissing = { navController.popBackStack() },
                onPaywall = { navController.navigate(NiyamRoutes.PAYWALL) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(NiyamRoutes.SETTINGS) {
            SettingsScreen(
                onOpenCurrentSadhana = { navController.navigate(NiyamRoutes.LIBRARY) },
                onOpenLanguage = { navController.navigate(NiyamRoutes.SETTINGS_LANGUAGE) },
                onOpenApps = { navController.navigate(NiyamRoutes.SETTINGS_APPS) },
                onOpenIntention = { navController.navigate(NiyamRoutes.SETTINGS_INTENTION) },
                onOpenPaywall = { navController.navigate(NiyamRoutes.PAYWALL) }
            )
        }
        composable(NiyamRoutes.SETTINGS_LANGUAGE) {
            val activity = LocalContext.current as? android.app.Activity
            LanguageSettingScreen(
                onSaved = {
                    navController.popBackStack()
                    // Chrome locale lives in attachBaseContext — recreate to re-wrap (SP-11).
                    activity?.recreate()
                },
                onPaywall = { navController.navigate(NiyamRoutes.PAYWALL) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(NiyamRoutes.SETTINGS_APPS) {
            BlockedAppsSettingScreen(
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(NiyamRoutes.SETTINGS_INTENTION) {
            IntentionSettingScreen(
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(NiyamRoutes.PAYWALL) {
            PaywallScreen(
                onUnlocked = { navController.popBackStack() },
                onClose = { navController.popBackStack() }
            )
        }
    }

        if (currentRoute in topLevel) {
            Box(Modifier.align(Alignment.BottomCenter)) {
                NiyamBottomBar(
                    currentRoute = currentRoute,
                    onSelect = { route ->
                        navController.navigate(route) {
                            popUpTo(NiyamRoutes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}
