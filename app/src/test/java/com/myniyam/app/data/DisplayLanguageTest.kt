package com.myniyam.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class DisplayLanguageTest {

    @Test
    fun `nine picker values exist`() {
        assertEquals(9, DisplayLanguage.entries.size)
    }

    @Test
    fun `each picker value resolves to spec mapping`() {
        val expected = mapOf(
            DisplayLanguage.DEVANAGARI_SANSKRIT to (Script.DEVANAGARI to MeaningLang.EN),
            DisplayLanguage.HINDI to (Script.DEVANAGARI to MeaningLang.HI),
            DisplayLanguage.MARATHI to (Script.DEVANAGARI to MeaningLang.MR),
            DisplayLanguage.ENGLISH to (Script.ROMAN to MeaningLang.EN),
            DisplayLanguage.TELUGU to (Script.TELUGU to MeaningLang.TE),
            DisplayLanguage.TAMIL to (Script.TAMIL to MeaningLang.TA),
            DisplayLanguage.KANNADA to (Script.KANNADA to MeaningLang.KN),
            DisplayLanguage.BENGALI to (Script.BENGALI to MeaningLang.BN),
            DisplayLanguage.GUJARATI to (Script.GUJARATI to MeaningLang.GU),
        )
        for ((lang, pair) in expected) {
            assertEquals("script for $lang", pair.first, lang.script)
            assertEquals("meaning for $lang", pair.second, lang.meaningLang)
        }
    }
}
