package com.myniyam.app.billing

import com.myniyam.app.data.DisplayLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EntitlementsTest {

    // --- state() ---

    @Test
    fun `unset trial is FREE`() {
        assertEquals(PremiumState.FREE, Entitlements.state(false, 0L, 100L))
    }

    @Test
    fun `trial day 0 is TRIAL`() {
        assertEquals(PremiumState.TRIAL, Entitlements.state(false, 100L, 100L))
    }

    @Test
    fun `trial day 6 is still TRIAL`() {
        assertEquals(PremiumState.TRIAL, Entitlements.state(false, 100L, 106L))
    }

    @Test
    fun `trial day 7 exactly is FREE (exclusive boundary)`() {
        assertEquals(PremiumState.FREE, Entitlements.state(false, 100L, 107L))
    }

    @Test
    fun `premiumActive overrides expired trial`() {
        assertEquals(PremiumState.PREMIUM, Entitlements.state(true, 100L, 200L))
    }

    @Test
    fun `clock rollback (today before start) is FREE`() {
        assertEquals(PremiumState.FREE, Entitlements.state(false, 100L, 99L))
    }

    // --- canUseMantra() ---

    @Test
    fun `canUseMantra free id in FREE state is true`() {
        assertTrue(Entitlements.canUseMantra(PremiumState.FREE, "gayatri", "gayatri"))
    }

    @Test
    fun `canUseMantra locked id in FREE state is false`() {
        assertFalse(Entitlements.canUseMantra(PremiumState.FREE, "some-locked-id", "gayatri"))
    }

    @Test
    fun `canUseMantra locked id equals current is true (grandfather)`() {
        assertTrue(Entitlements.canUseMantra(PremiumState.FREE, "some-locked-id", "some-locked-id"))
    }

    @Test
    fun `canUseMantra locked id in TRIAL state is true`() {
        assertTrue(Entitlements.canUseMantra(PremiumState.TRIAL, "some-locked-id", "gayatri"))
    }

    // --- canUseLanguage() ---

    @Test
    fun `canUseLanguage TELUGU in FREE state is false`() {
        assertFalse(Entitlements.canUseLanguage(PremiumState.FREE, DisplayLanguage.TELUGU, DisplayLanguage.ENGLISH))
    }

    @Test
    fun `canUseLanguage TELUGU equals current is true (grandfather)`() {
        assertTrue(Entitlements.canUseLanguage(PremiumState.FREE, DisplayLanguage.TELUGU, DisplayLanguage.TELUGU))
    }

    @Test
    fun `canUseLanguage HINDI in FREE state is true`() {
        assertTrue(Entitlements.canUseLanguage(PremiumState.FREE, DisplayLanguage.HINDI, DisplayLanguage.ENGLISH))
    }

    // --- trialDaysLeft() ---

    @Test
    fun `trialDaysLeft unset is 0`() {
        assertEquals(0, Entitlements.trialDaysLeft(0L, 100L))
    }

    @Test
    fun `trialDaysLeft day 0 is 7`() {
        assertEquals(7, Entitlements.trialDaysLeft(100L, 100L))
    }

    @Test
    fun `trialDaysLeft day 6 is 1`() {
        assertEquals(1, Entitlements.trialDaysLeft(100L, 106L))
    }

    @Test
    fun `trialDaysLeft day 7 is 0`() {
        assertEquals(0, Entitlements.trialDaysLeft(100L, 107L))
    }

    @Test
    fun `trialDaysLeft clock rollback is 0`() {
        assertEquals(0, Entitlements.trialDaysLeft(100L, 99L))
    }
}
