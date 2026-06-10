package com.myniyam.app.progress

import com.myniyam.app.data.Intention
import com.myniyam.app.data.MantraRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import java.io.File

class NextSadhanaTest {

    @Before
    fun loadCatalog() {
        MantraRepository.resetForTest()
        val json = File("src/main/assets/content/mantras.json").readText()
        check(MantraRepository.initFromJson(json))
    }

    @Test
    fun `excludes current and completed, keeps priority order`() {
        val result = NextSadhana.candidates(
            intention = Intention.CALM,
            completed = setOf("mahamrityunjaya"),
            currentId = "om-sahanavavatu"
        ).map { it.id }
        assertEquals(listOf("om-namah-shivaya", "gita-2-70", "twameva-mata"), result)
    }

    @Test
    fun `backfills from the catalog when the intention list is exhausted`() {
        val calmIds = setOf(
            "mahamrityunjaya", "om-sahanavavatu", "om-namah-shivaya", "gita-2-70", "twameva-mata"
        )
        val result = NextSadhana.candidates(
            intention = Intention.CALM,
            completed = calmIds,
            currentId = "om"
        ).map { it.id }
        assertEquals(3, result.size)
        result.forEach { id ->
            assertFalse("must not suggest completed/current", id in calmIds || id == "om")
        }
    }

    @Test
    fun `always returns at most three`() {
        assertEquals(3, NextSadhana.candidates(Intention.SADHANA, emptySet(), "gayatri").size)
    }
}
