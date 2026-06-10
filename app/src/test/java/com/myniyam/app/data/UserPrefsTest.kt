package com.myniyam.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class UserPrefsTest {

    @Test
    fun `defaults match the pre-SP3 hardcoded behavior`() {
        val s = UserPrefs.Snapshot.DEFAULTS
        assertFalse(s.onboardingComplete)
        assertEquals("gayatri", s.currentMantraId)
        assertEquals(DisplayLanguage.DEVANAGARI_SANSKRIT, s.displayLanguage)
        assertEquals(
            setOf("com.instagram.android", "com.facebook.katana", "com.google.android.youtube"),
            s.blockedPackages
        )
    }

    @Test
    fun `fromRaw maps stored values`() {
        val s = UserPrefs.Snapshot.fromRaw(
            onboardingComplete = true,
            mantraId = "mahamrityunjaya",
            language = "TAMIL",
            blocked = setOf("com.instagram.android")
        )
        assertEquals(true, s.onboardingComplete)
        assertEquals("mahamrityunjaya", s.currentMantraId)
        assertEquals(DisplayLanguage.TAMIL, s.displayLanguage)
        assertEquals(setOf("com.instagram.android"), s.blockedPackages)
    }

    @Test
    fun `fromRaw falls back on unknown language and null fields`() {
        val s = UserPrefs.Snapshot.fromRaw(
            onboardingComplete = null,
            mantraId = null,
            language = "KLINGON",
            blocked = null
        )
        assertEquals(UserPrefs.Snapshot.DEFAULTS, s)
    }

    @Test
    fun `snapshot is replaced atomically for tests`() {
        UserPrefs.setSnapshotForTest(UserPrefs.Snapshot.DEFAULTS.copy(currentMantraId = "om"))
        assertEquals("om", UserPrefs.snapshot().currentMantraId)
        UserPrefs.resetForTest()
        assertEquals(UserPrefs.Snapshot.DEFAULTS, UserPrefs.snapshot())
    }
}
