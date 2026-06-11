package com.myniyam.app.ui

import android.content.Context
import android.content.res.Configuration
import com.myniyam.app.data.DisplayLanguage
import com.myniyam.app.data.UserPrefs
import java.util.Locale

/**
 * Maps the IN-APP language choice to a chrome locale (SP-11 spec §2) and wraps
 * Contexts so string resources resolve in that language. Sanskrit users get
 * Hindi chrome — the mantra text itself stays Sanskrit via the Script system.
 */
object LocaleBridge {

    fun localeFor(lang: DisplayLanguage): Locale = when (lang) {
        DisplayLanguage.ENGLISH -> Locale("en")
        DisplayLanguage.HINDI -> Locale("hi")
        DisplayLanguage.DEVANAGARI_SANSKRIT -> Locale("hi")
        DisplayLanguage.MARATHI -> Locale("mr")
        DisplayLanguage.TELUGU -> Locale("te")
        DisplayLanguage.TAMIL -> Locale("ta")
        DisplayLanguage.KANNADA -> Locale("kn")
        DisplayLanguage.BENGALI -> Locale("bn")
        DisplayLanguage.GUJARATI -> Locale("gu")
    }

    /** Wrap [base] so resources resolve in the user's chosen chrome language. */
    fun wrap(base: Context): Context {
        val locale = localeFor(UserPrefs.snapshot().displayLanguage)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }
}
