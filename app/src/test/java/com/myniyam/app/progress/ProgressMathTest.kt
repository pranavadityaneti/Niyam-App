package com.myniyam.app.progress

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressMathTest {

    @Test
    fun `empty days means zero streak`() {
        assertEquals(0, ProgressMath.streak(emptySet(), today = 100))
    }

    @Test
    fun `read today only is streak 1`() {
        assertEquals(1, ProgressMath.streak(setOf(100L), today = 100))
    }

    @Test
    fun `read yesterday but not today keeps streak alive`() {
        assertEquals(1, ProgressMath.streak(setOf(99L), today = 100))
    }

    @Test
    fun `consecutive days count back from today`() {
        assertEquals(4, ProgressMath.streak(setOf(97L, 98L, 99L, 100L), today = 100))
    }

    @Test
    fun `gap breaks the streak`() {
        assertEquals(2, ProgressMath.streak(setOf(96L, 99L, 100L), today = 100))
    }

    @Test
    fun `dayN is capped at the threshold`() {
        assertEquals(3, ProgressMath.dayN(distinctDays = 3, capM = 14))
        assertEquals(14, ProgressMath.dayN(distinctDays = 20, capM = 14))
    }

    @Test
    fun `completion triggers exactly at the threshold boundary`() {
        assertEquals(false, ProgressMath.isComplete(distinctDays = 13, threshold = 14))
        assertEquals(true, ProgressMath.isComplete(distinctDays = 14, threshold = 14))
        assertEquals(true, ProgressMath.isComplete(distinctDays = 15, threshold = 14))
    }
}
