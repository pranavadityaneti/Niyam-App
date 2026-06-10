package com.myniyam.app.data

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ContentValidationTest {

    private val assetFile = File("src/main/assets/content/mantras.json")
    private val catalog: MantraCatalog by lazy {
        Json { ignoreUnknownKeys = true }.decodeFromString(assetFile.readText())
    }

    @Test
    fun `asset exists and parses`() {
        assertTrue("asset missing at ${assetFile.absolutePath}", assetFile.exists())
        assertEquals(1, catalog.schemaVersion)
        assertTrue("contentVersion must be set", catalog.contentVersion.isNotBlank())
    }

    @Test
    fun `entry count`() {
        // Flipped to assertEquals(26, ...) in Task 12 once all batches land.
        assertTrue("at least one entry", catalog.mantras.isNotEmpty())
    }

    @Test
    fun `ids are unique kebab-case`() {
        val ids = catalog.mantras.map { it.id }
        assertEquals("duplicate ids", ids.size, ids.toSet().size)
        ids.forEach { id ->
            assertTrue("id '$id' must be lowercase-kebab", id.matches(Regex("[a-z0-9]+(-[a-z0-9]+)*")))
        }
    }

    @Test
    fun `every entry has all scripts non-empty`() {
        catalog.mantras.forEach { m ->
            Script.entries.forEach { s ->
                assertTrue("${m.id}: blank script $s", m.text.forScript(s).isNotBlank())
            }
        }
    }

    @Test
    fun `every entry has all meanings non-empty`() {
        catalog.mantras.forEach { m ->
            MeaningLang.entries.forEach { l ->
                assertTrue("${m.id}: blank meaning $l", m.meaning.forLang(l).isNotBlank())
            }
        }
    }

    @Test
    fun `every entry has source attribution and at least one sourceRef`() {
        catalog.mantras.forEach { m ->
            assertTrue("${m.id}: blank source", m.source.isNotBlank())
            assertTrue("${m.id}: needs >=1 sourceRef", m.sourceRefs.isNotEmpty())
        }
    }

    @Test
    fun `every entry has at least one intention and positive numbers`() {
        catalog.mantras.forEach { m ->
            assertTrue("${m.id}: needs >=1 intention", m.intentions.isNotEmpty())
            assertTrue("${m.id}: read seconds > 0", m.estimatedReadSeconds > 0)
            assertTrue("${m.id}: threshold > 0", m.completionThresholdDays > 0)
        }
    }

    @Test
    fun `asset stays within size budget`() {
        assertTrue("mantras.json must be <= 400KB", assetFile.length() <= 400 * 1024)
    }

    private val scriptBlocks = mapOf(
        Script.DEVANAGARI to 0x0900..0x097F,
        Script.TELUGU to 0x0C00..0x0C7F,
        Script.TAMIL to 0x0B80..0x0BFF,
        Script.KANNADA to 0x0C80..0x0CFF,
        Script.BENGALI to 0x0980..0x09FF,
        Script.GUJARATI to 0x0A80..0x0AFF,
    )

    // Shared characters allowed in any script field: whitespace, ASCII
    // punctuation/digits (danda maps to "." in most scripts), Devanagari
    // dandas (। ॥), ZWJ/ZWNJ, and the calibrated Tamil visarga ꞉ (U+A789).
    // ASCII letters are NOT shared — Latin text in an Indic field is a paste error.
    private fun isSharedChar(c: Char): Boolean =
        c.isWhitespace() || (c.code < 0x80 && !c.isLetter()) || c == '।' || c == '॥' ||
            c.code == 0x200C || c.code == 0x200D || c.code == 0xA789

    @Test
    fun `script fields contain only their own script`() {
        catalog.mantras.forEach { m ->
            scriptBlocks.forEach { (script, range) ->
                m.text.forScript(script).forEach { c ->
                    assertTrue(
                        "${m.id}.$script: stray char '$c' (U+${"%04X".format(c.code)})",
                        isSharedChar(c) || c.code in range
                    )
                }
            }
        }
    }

    @Test
    fun `meanings have plausible length`() {
        catalog.mantras.forEach { m ->
            MeaningLang.entries.forEach { l ->
                assertTrue(
                    "${m.id}: meaning $l suspiciously short (truncation?)",
                    m.meaning.forLang(l).length >= 20
                )
            }
        }
    }
}
