package com.myniyam.app.data

/**
 * Stand-in for the user's sadhana selection until onboarding (SP-3) exists.
 * SP-3 replaces these constants with persisted user choices.
 */
object CurrentSadhana {
    const val MANTRA_ID: String = "gayatri"
    val LANGUAGE: DisplayLanguage = DisplayLanguage.DEVANAGARI_SANSKRIT
}
