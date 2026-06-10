package com.myniyam.app.library

import com.myniyam.app.data.Deity
import com.myniyam.app.data.Intention
import com.myniyam.app.data.MantraRepository
import com.myniyam.app.data.SourceCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class LibraryFiltersTest {

    @Before
    fun loadCatalog() {
        MantraRepository.resetForTest()
        check(MantraRepository.initFromJson(File("src/main/assets/content/mantras.json").readText()))
    }

    @Test
    fun `length buckets have correct boundaries`() {
        assertEquals(LengthBucket.UNDER_30S, LengthBucket.of(29))
        assertEquals(LengthBucket.S30_TO_60, LengthBucket.of(30))
        assertEquals(LengthBucket.S30_TO_60, LengthBucket.of(60))
        assertEquals(LengthBucket.OVER_60S, LengthBucket.of(61))
    }

    @Test
    fun `empty selection passes everything through in catalog order`() {
        val all = MantraRepository.all()
        assertEquals(all.map { it.id }, LibraryFilters.apply(all, LibraryFilters.Selection()).map { it.id })
    }

    @Test
    fun `category filter narrows to the gita eight`() {
        val result = LibraryFilters.apply(
            MantraRepository.all(),
            LibraryFilters.Selection(category = SourceCategory.GITA)
        )
        assertEquals(8, result.size)
        assertTrue(result.all { it.sourceCategory == SourceCategory.GITA })
    }

    @Test
    fun `dimensions combine with AND`() {
        val result = LibraryFilters.apply(
            MantraRepository.all(),
            LibraryFilters.Selection(category = SourceCategory.GITA, intention = Intention.CALM)
        )
        assertEquals(listOf("gita-2-14", "gita-2-70"), result.map { it.id })
    }

    @Test
    fun `deity and length filters work`() {
        val shiva = LibraryFilters.apply(MantraRepository.all(), LibraryFilters.Selection(deity = Deity.SHIVA))
        assertEquals(setOf("mahamrityunjaya", "om-namah-shivaya"), shiva.map { it.id }.toSet())
        val long = LibraryFilters.apply(MantraRepository.all(), LibraryFilters.Selection(length = LengthBucket.OVER_60S))
        assertEquals(setOf("hanuman-chalisa-opening", "vishnu-sahasranama-opening", "lalita-sahasranama-opening"), long.map { it.id }.toSet())
    }
}
