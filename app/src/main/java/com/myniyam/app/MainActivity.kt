package com.myniyam.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.myniyam.app.backend.PracticeSync
import com.myniyam.app.backend.RemoteConfig
import com.myniyam.app.data.ThemePref
import com.myniyam.app.data.UserPrefs
import kotlinx.coroutines.launch
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
        RemoteConfig.ensureLoaded(this)
        lifecycleScope.launch { RemoteConfig.refresh(this@MainActivity) }
        // Billing self-heal (audit C): re-query owned purchases on every launch and
        // acknowledge/verify any that slipped through. A purchase whose original
        // acknowledgement failed (network blip) would otherwise be auto-refunded by
        // Play after 3 days; this makes acknowledgement durable. No-op in debug
        // (sandbox gateway) and for free users with nothing to restore.
        lifecycleScope.launch {
            try { com.myniyam.app.billing.Billing.gateway.restorePurchases(this@MainActivity) } catch (e: Exception) {}
        }
        val s = UserPrefs.snapshot()
        if (s.onboardingComplete && s.trialStartEpochDay == 0L) {
            runBlocking { UserPrefs.startTrial(this@MainActivity, java.time.LocalDate.now().toEpochDay()) }
        }
        // Event-anchored trial reminder (audit B2): schedule the day-6 reminder for
        // the actual trial-end window so a batched daily worker can't miss it.
        UserPrefs.snapshot().let { snap ->
            if (snap.trialStartEpochDay > 0L && !snap.trialReminderShown) {
                com.myniyam.app.billing.TrialReminderWorker.scheduleExact(this, snap.trialStartEpochDay)
            }
        }
        ThemeState.set(UserPrefs.snapshot().themePref)
        val start = if (UserPrefs.snapshot().onboardingComplete) NiyamRoutes.HOME else NiyamRoutes.WELCOME
        // OTA force-update gate: if this build is below the server minimum, block.
        val forceUpdate = BuildConfig.VERSION_CODE < RemoteConfig.minSupportedVersionCode()
        val updateMsg = RemoteConfig.updateMessage()
            ?: "A new version of Niyam is available. Please update to keep practising."
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
                if (forceUpdate) {
                    com.myniyam.app.ui.screens.ForceUpdateScreen(updateMsg)
                } else {
                    AppNavHost(startDestination = start)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Push local practice + favourites to the server when leaving the app
        // (P5). One wiring point captures all in-session changes. Best-effort;
        // no-op when signed out. Never on the engine path.
        lifecycleScope.launch { PracticeSync.push(this@MainActivity) }
    }
}
