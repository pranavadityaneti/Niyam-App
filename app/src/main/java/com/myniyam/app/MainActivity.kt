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
import com.myniyam.app.ui.AppNavHost
import com.myniyam.app.ui.NiyamRoutes
import com.myniyam.app.ui.theme.NiyamTheme
import com.myniyam.app.ui.theme.ThemeState

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
        UserPrefs.ensureLoaded(this)
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
