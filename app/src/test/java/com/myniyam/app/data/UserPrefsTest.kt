package com.myniyam.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
        assertEquals(Intention.SADHANA, s.selectedIntention)
        assertEquals(0L, s.sadhanaStartEpochDay)
        assertTrue(s.completedMantraIds.isEmpty())
        assertFalse(s.pendingCelebration)
    }

    @Test
    fun `fromRaw maps stored values`() {
        val s = UserPrefs.Snapshot.fromRaw(
            onboardingComplete = true,
            mantraId = "mahamrityunjaya",
            language = "TAMIL",
            blocked = setOf("com.instagram.android"),
            intention = "CALM",
            sadhanaStart = 20600L,
            completed = setOf("om"),
            pendingCelebration = true
        )
        assertEquals(true, s.onboardingComplete)
        assertEquals("mahamrityunjaya", s.currentMantraId)
        assertEquals(DisplayLanguage.TAMIL, s.displayLanguage)
        assertEquals(setOf("com.instagram.android"), s.blockedPackages)
        assertEquals(Intention.CALM, s.selectedIntention)
        assertEquals(20600L, s.sadhanaStartEpochDay)
        assertEquals(setOf("om"), s.completedMantraIds)
        assertTrue(s.pendingCelebration)
    }

    @Test
    fun `fromRaw falls back on unknown language and null fields`() {
        val s = UserPrefs.Snapshot.fromRaw(
            onboardingComplete = null,
            mantraId = null,
            language = "KLINGON",
            blocked = null,
            intention = "UNKNOWN_X"
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

    @Test
    fun `completion bookkeeping is representable in the snapshot`() {
        UserPrefs.setSnapshotForTest(
            UserPrefs.Snapshot.DEFAULTS.copy(
                completedMantraIds = setOf("gayatri"),
                pendingCelebration = true
            )
        )
        assertTrue("gayatri" in UserPrefs.snapshot().completedMantraIds)
        assertTrue(UserPrefs.snapshot().pendingCelebration)
        UserPrefs.resetForTest()
    }

    @Test
    fun `setCurrentMantra semantics are representable`() {
        UserPrefs.setSnapshotForTest(
            UserPrefs.Snapshot.DEFAULTS.copy(
                currentMantraId = "vakratunda",
                sadhanaStartEpochDay = 20620L,
                pendingCelebration = false
            )
        )
        val s = UserPrefs.snapshot()
        assertEquals("vakratunda", s.currentMantraId)
        assertEquals(20620L, s.sadhanaStartEpochDay)
        assertFalse(s.pendingCelebration)
        UserPrefs.resetForTest()
    }

    @Test
    fun `intention round-trips by name`() {
        Intention.entries.forEach { intention ->
            val s = UserPrefs.Snapshot.fromRaw(null, null, null, null, intention = intention.name)
            assertEquals(intention, s.selectedIntention)
        }
    }
}
