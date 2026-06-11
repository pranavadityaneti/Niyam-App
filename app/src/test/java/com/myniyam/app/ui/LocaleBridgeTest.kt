package com.myniyam.app.ui

import com.myniyam.app.data.DisplayLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class LocaleBridgeTest {

    @Test
    fun `every display language maps to its chrome locale`() {
        val expected = mapOf(
            DisplayLanguage.ENGLISH to "en",
            DisplayLanguage.HINDI to "hi",
            DisplayLanguage.DEVANAGARI_SANSKRIT to "hi", // Sanskrit chrome = Hindi (spec §2)
            DisplayLanguage.MARATHI to "mr",
            DisplayLanguage.TELUGU to "te",
            DisplayLanguage.TAMIL to "ta",
            DisplayLanguage.KANNADA to "kn",
            DisplayLanguage.BENGALI to "bn",
            DisplayLanguage.GUJARATI to "gu"
        )
        DisplayLanguage.entries.forEach { lang ->
            assertEquals(
                "chrome locale for $lang",
                expected.getValue(lang),
                LocaleBridge.localeFor(lang).language
            )
        }
        // The map covers every enum entry — a new language must get a chrome ruling.
        assertEquals(DisplayLanguage.entries.size, expected.size)
    }
}
