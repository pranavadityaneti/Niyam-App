package com.myniyam.app.data

/**
 * The user's active sadhana selection, backed by UserPrefs (SP-3).
 * Defaults (pre-onboarding) match the previous hardcoded values.
 */
object CurrentSadhana {
    val MANTRA_ID: String get() = UserPrefs.snapshot().currentMantraId
    val LANGUAGE: DisplayLanguage get() = UserPrefs.snapshot().displayLanguage
}
