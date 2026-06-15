package com.myniyam.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MantraRepositoryTest {

    private val validJson = """
    {
      "schemaVersion": 1,
      "contentVersion": "2026-06-10.1",
      "mantras": [{
        "id": "om",
        "canonicalName": "Om (Pranava)",
        "name": {"devanagari": "ॐ", "telugu": "ఓం", "tamil": "ஓம்",
                 "kannada": "ಓಂ", "bengali": "ওঁ", "gujarati": "ૐ", "roman": "Om (Pranava)"},
        "originalLanguage": "sanskrit",
        "text": {"devanagari": "ॐ", "telugu": "ఓం", "tamil": "ஓம்",
                 "kannada": "ಓಂ", "bengali": "ওঁ", "gujarati": "ૐ", "roman": "Om"},
        "meaning": {"en": "e", "hi": "h", "te": "t", "ta": "a",
                    "kn": "k", "mr": "m", "bn": "b", "gu": "g"},
        "source": "Mandukya Upanishad",
        "sourceLabel": {"devanagari": "माण्डूक्य उपनिषद्", "telugu": "మాండూక్య ఉపనిషద్", "tamil": "மாண்டூக்ய உபநிஷத்",
                        "kannada": "ಮಾಂಡೂಕ್ಯ ಉಪನಿಷದ್", "bengali": "মাণ্ডূক্য উপনিষদ্", "gujarati": "માણ્ડૂક্ય ઉપનিষદ্", "roman": "Mandukya Upanishad"},
        "sourceRefs": ["https://example.org/om"],
        "deity": "universal",
        "intentions": ["calm"],
        "estimatedReadSeconds": 5,
        "completionThresholdDays": 14
      }]
    }
    """.trimIndent()

    @Before
    fun reset() {
        MantraRepository.resetForTest()
    }

    @Test
    fun `initFromJson succeeds on valid catalog`() {
        assertTrue(MantraRepository.initFromJson(validJson))
        assertEquals(1, MantraRepository.all().size)
    }

    @Test
    fun `byId returns entry and null for unknown`() {
        MantraRepository.initFromJson(validJson)
        assertEquals("Om (Pranava)", MantraRepository.byId("om")?.canonicalName)
        assertNull(MantraRepository.byId("nope"))
    }

    @Test
    fun `initFromJson fails on corrupt json without throwing`() {
        assertFalse(MantraRepository.initFromJson("{ not json"))
        assertTrue(MantraRepository.all().isEmpty())
    }

    @Test
    fun `initFromJson rejects wrong schemaVersion`() {
        val wrong = validJson.replace("\"schemaVersion\": 1", "\"schemaVersion\": 99")
        assertFalse(MantraRepository.initFromJson(wrong))
    }

    @Test
    fun `displayMantra falls back to built-in om when not loaded`() {
        val m = MantraRepository.displayMantra("gayatri")
        assertEquals("om", m.id)
        assertEquals("ॐ", m.text.devanagari)
    }

    @Test
    fun `displayMantra returns requested entry when loaded`() {
        MantraRepository.initFromJson(validJson)
        assertEquals("om", MantraRepository.displayMantra("om").id)
    }

    @Test
    fun `displayMantra falls back for unknown id even when loaded`() {
        MantraRepository.initFromJson(validJson)
        assertEquals("om", MantraRepository.displayMantra("does-not-exist").id)
    }
}
