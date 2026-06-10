package com.myniyam.app.data

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class MantraModelTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val sampleJson = """
    {
      "schemaVersion": 1,
      "contentVersion": "2026-06-10.1",
      "mantras": [{
        "id": "om",
        "canonicalName": "Om (Pranava)",
        "originalLanguage": "sanskrit",
        "text": {
          "devanagari": "ॐ", "telugu": "ఓం", "tamil": "ஓம்",
          "kannada": "ಓಂ", "bengali": "ওঁ", "gujarati": "ૐ", "roman": "Om"
        },
        "meaning": {
          "en": "e", "hi": "h", "te": "t", "ta": "a",
          "kn": "k", "mr": "m", "bn": "b", "gu": "g"
        },
        "source": "Mandukya Upanishad",
        "sourceRefs": ["https://example.org/om"],
        "deity": "universal",
        "intentions": ["calm", "sadhana"],
        "estimatedReadSeconds": 5,
        "completionThresholdDays": 14
      }]
    }
    """.trimIndent()

    @Test
    fun `catalog decodes from json`() {
        val catalog = json.decodeFromString<MantraCatalog>(sampleJson)
        assertEquals(1, catalog.schemaVersion)
        assertEquals("2026-06-10.1", catalog.contentVersion)
        assertEquals(1, catalog.mantras.size)
    }

    @Test
    fun `entry fields decode with enum mapping`() {
        val m = json.decodeFromString<MantraCatalog>(sampleJson).mantras.first()
        assertEquals("om", m.id)
        assertEquals(OriginalLanguage.SANSKRIT, m.originalLanguage)
        assertEquals(Deity.UNIVERSAL, m.deity)
        assertEquals(listOf(Intention.CALM, Intention.SADHANA), m.intentions)
        assertEquals(5, m.estimatedReadSeconds)
        assertEquals(14, m.completionThresholdDays)
    }

    @Test
    fun `text forScript returns every script`() {
        val t = json.decodeFromString<MantraCatalog>(sampleJson).mantras.first().text
        assertEquals("ॐ", t.forScript(Script.DEVANAGARI))
        assertEquals("ఓం", t.forScript(Script.TELUGU))
        assertEquals("ஓம்", t.forScript(Script.TAMIL))
        assertEquals("ಓಂ", t.forScript(Script.KANNADA))
        assertEquals("ওঁ", t.forScript(Script.BENGALI))
        assertEquals("ૐ", t.forScript(Script.GUJARATI))
        assertEquals("Om", t.forScript(Script.ROMAN))
    }

    @Test
    fun `meaning forLang returns every language`() {
        val m = json.decodeFromString<MantraCatalog>(sampleJson).mantras.first().meaning
        assertEquals("e", m.forLang(MeaningLang.EN))
        assertEquals("h", m.forLang(MeaningLang.HI))
        assertEquals("t", m.forLang(MeaningLang.TE))
        assertEquals("a", m.forLang(MeaningLang.TA))
        assertEquals("k", m.forLang(MeaningLang.KN))
        assertEquals("m", m.forLang(MeaningLang.MR))
        assertEquals("b", m.forLang(MeaningLang.BN))
        assertEquals("g", m.forLang(MeaningLang.GU))
    }
}
