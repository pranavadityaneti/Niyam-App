package com.myniyam.app.data

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

class StarterMantrasTest {

    @Before
    fun loadRealCatalog() {
        MantraRepository.resetForTest()
        val json = File("src/main/assets/content/mantras.json").readText()
        check(MantraRepository.initFromJson(json))
    }

    @Test
    fun `each intention returns its top-3 curated entries in order`() {
        assertEquals(listOf("gita-2-47", "gita-6-5", "gita-2-14"), ids(Intention.FOCUS))
        assertEquals(listOf("mahamrityunjaya", "om-sahanavavatu", "om-namah-shivaya"), ids(Intention.CALM))
        assertEquals(listOf("gayatri", "vakratunda", "saraswati-vandana"), ids(Intention.SADHANA))
        assertEquals(listOf("gita-4-7-8", "gita-18-66", "gita-3-35"), ids(Intention.DHARMA))
        assertEquals(
            listOf("hanuman-chalisa-opening", "vishnu-sahasranama-opening", "lalita-sahasranama-opening"),
            ids(Intention.DEVOTION)
        )
    }

    @Test
    fun `unknown ids are dropped and backfilled from the priority list`() {
        MantraRepository.resetForTest()
        val json = File("src/main/assets/content/mantras.json").readText()
            .replace("\"id\": \"gita-2-47\"", "\"id\": \"renamed-away\"")
        check(MantraRepository.initFromJson(json))
        assertEquals(listOf("gita-6-5", "gita-2-14", "asato-ma"), ids(Intention.FOCUS))
    }

    private fun ids(intention: Intention) =
        StarterMantras.forIntention(intention).map { it.id }
}
