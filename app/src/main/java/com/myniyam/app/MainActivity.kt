package com.myniyam.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import com.myniyam.app.data.ThemePref
import com.myniyam.app.data.UserPrefs
import kotlinx.coroutines.runBlocking
import com.myniyam.app.ui.AppNavHost
import com.myniyam.app.ui.NiyamRoutes
import com.myniyam.app.ui.theme.NiyamTheme
import com.myniyam.app.ui.theme.ThemeState

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: android.content.Context) {
        // Chrome language follows the in-app choice (SP-11). ensureLoaded is
        // idempotent and already the app's blocking warm-up pattern.
        UserPrefs.ensureLoaded(newBase)
        super.attachBaseContext(com.myniyam.app.ui.LocaleBridge.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
        UserPrefs.ensureLoaded(this)
        val s = UserPrefs.snapshot()
        if (s.onboardingComplete && s.trialStartEpochDay == 0L) {
            runBlocking { UserPrefs.startTrial(this@MainActivity, java.time.LocalDate.now().toEpochDay()) }
        }
        ThemeState.set(UserPrefs.snapshot().themePref)
        val start = if (UserPrefs.snapshot().onboardingComplete) NiyamRoutes.HOME else NiyamRoutes.WELCOME
        setContent {
            NiyamTheme {
                val dark = when (ThemeState.pref) {
                    ThemePref.LIGHT -> false
                    ThemePref.DARK -> true
                    ThemePref.SYSTEM -> isSystemInDarkTheme()
                }
                SideEffect {
                    WindowCompat.getInsetsController(window, window.decorView)
                        .isAppearanceLightStatusBars = !dark
                }
                AppNavHost(startDestination = start)
            }
        }
    }
}
