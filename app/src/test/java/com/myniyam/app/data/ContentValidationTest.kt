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
}
