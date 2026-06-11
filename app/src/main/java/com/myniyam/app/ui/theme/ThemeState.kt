package com.myniyam.app.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.myniyam.app.data.ThemePref

/**
 * Compose-observable mirror of UserPrefs.themePref (SP-7). The DataStore
 * write in UserPrefs.setThemePref is the source of truth; this mirror is
 * updated alongside it so NiyamTheme recomposes the instant the toggle flips.
 * MainActivity seeds it from the snapshot after ensureLoaded() on cold start.
 */
object ThemeState {
    var pref: ThemePref by mutableStateOf(ThemePref.LIGHT)
        private set

    fun set(value: ThemePref) { pref = value }
}
