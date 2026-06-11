package com.myniyam.app.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UnlockGraceTest {

    @Before
    fun reset() {
        UnlockGrace.clear()
    }

    @Test
    fun `no grant means not active`() {
        assertFalse(UnlockGrace.isActive("com.google.android.youtube", 1_000L))
    }

    @Test
    fun `active immediately after grant`() {
        UnlockGrace.grant("com.google.android.youtube", 1_000L)
        assertTrue(UnlockGrace.isActive("com.google.android.youtube", 1_000L))
    }

    @Test
    fun `active just before the window closes`() {
        UnlockGrace.grant("com.google.android.youtube", 1_000L)
        assertTrue(UnlockGrace.isActive("com.google.android.youtube", 1_000L + UnlockGrace.GRACE_MS - 1))
    }

    @Test
    fun `inactive exactly at the window boundary`() {
        UnlockGrace.grant("com.google.android.youtube", 1_000L)
        assertFalse(UnlockGrace.isActive("com.google.android.youtube", 1_000L + UnlockGrace.GRACE_MS))
    }

    @Test
    fun `grace is per package`() {
        UnlockGrace.grant("com.google.android.youtube", 1_000L)
        assertFalse(UnlockGrace.isActive("com.instagram.android", 1_000L))
    }

    @Test
    fun `re-grant resets the window`() {
        UnlockGrace.grant("com.google.android.youtube", 1_000L)
        val later = 1_000L + UnlockGrace.GRACE_MS - 1
        UnlockGrace.grant("com.google.android.youtube", later)
        assertTrue(UnlockGrace.isActive("com.google.android.youtube", later + UnlockGrace.GRACE_MS - 1))
    }

    @Test
    fun `grace window is five minutes`() {
        assertTrue(UnlockGrace.GRACE_MS == 5 * 60_000L)
    }
}
