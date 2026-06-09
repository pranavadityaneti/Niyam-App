package com.myniyam.app.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlockListTest {

    @Test
    fun `matches returns true for instagram package`() {
        assertTrue(BlockList.matches("com.instagram.android"))
    }

    @Test
    fun `matches returns true for facebook main app package`() {
        assertTrue(BlockList.matches("com.facebook.katana"))
    }

    @Test
    fun `matches returns true for youtube main app package`() {
        assertTrue(BlockList.matches("com.google.android.youtube"))
    }

    @Test
    fun `matches returns false for messenger`() {
        assertFalse(BlockList.matches("com.facebook.orca"))
    }

    @Test
    fun `matches returns false for youtube music`() {
        assertFalse(BlockList.matches("com.google.android.apps.youtube.music"))
    }

    @Test
    fun `matches returns false for facebook lite`() {
        assertFalse(BlockList.matches("com.facebook.lite"))
    }

    @Test
    fun `matches returns false for chrome`() {
        assertFalse(BlockList.matches("com.android.chrome"))
    }

    @Test
    fun `matches returns false for empty string`() {
        assertFalse(BlockList.matches(""))
    }
}
