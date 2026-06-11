package com.myniyam.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.myniyam.app.data.UserPrefs
import com.myniyam.app.ui.AppNavHost
import com.myniyam.app.ui.NiyamRoutes
import com.myniyam.app.ui.theme.NiyamTheme
import com.myniyam.app.ui.theme.ThemeState

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UserPrefs.ensureLoaded(this)
        ThemeState.set(UserPrefs.snapshot().themePref)
        val start = if (UserPrefs.snapshot().onboardingComplete) NiyamRoutes.HOME else NiyamRoutes.WELCOME
        setContent {
            NiyamTheme {
                AppNavHost(startDestination = start)
            }
        }
    }
}
