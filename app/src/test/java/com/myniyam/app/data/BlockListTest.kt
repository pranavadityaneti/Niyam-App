package com.myniyam.app.data

import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BlockListTest {

    @Before fun setUp() = UserPrefs.resetForTest()
    @After fun tearDown() = UserPrefs.resetForTest()

    @Test fun `matches returns true for instagram package`() { assertTrue(BlockList.matches("com.instagram.android")) }
    @Test fun `matches returns true for facebook main app package`() { assertTrue(BlockList.matches("com.facebook.katana")) }
    @Test fun `matches returns true for youtube main app package`() { assertTrue(BlockList.matches("com.google.android.youtube")) }
    @Test fun `matches returns false for messenger`() { assertFalse(BlockList.matches("com.facebook.orca")) }
    @Test fun `matches returns false for youtube music`() { assertFalse(BlockList.matches("com.google.android.apps.youtube.music")) }
    @Test fun `matches returns false for facebook lite`() { assertFalse(BlockList.matches("com.facebook.lite")) }
    @Test fun `matches returns false for chrome`() { assertFalse(BlockList.matches("com.android.chrome")) }
    @Test fun `matches returns false for empty string`() { assertFalse(BlockList.matches("")) }

    @Test
    fun `user-selected set overrides defaults`() {
        UserPrefs.setSnapshotForTest(
            UserPrefs.Snapshot.DEFAULTS.copy(blockedPackages = setOf("com.twitter.android"))
        )
        assertTrue(BlockList.matches("com.twitter.android"))
        assertFalse(BlockList.matches("com.instagram.android"))
    }

    @Test
    fun `defaults apply before any prefs load`() {
        assertTrue(BlockList.matches("com.instagram.android"))
    }
}
