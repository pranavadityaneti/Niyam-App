package com.myniyam.app.billing

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrialReminderTest {

    private val start = 1_000L

    @Test
    fun `reminds on day 6 — one day left`() {
        assertTrue(TrialReminder.shouldRemind(false, start, start + 6, alreadyShown = false))
    }

    @Test
    fun `does not remind mid-trial`() {
        assertFalse(TrialReminder.shouldRemind(false, start, start + 3, alreadyShown = false))
    }

    @Test
    fun `does not remind after trial ends`() {
        assertFalse(TrialReminder.shouldRemind(false, start, start + 7, alreadyShown = false))
    }

    @Test
    fun `never reminds premium users`() {
        assertTrue(Entitlements.state(true, start, start + 6) == PremiumState.PREMIUM)
        assertFalse(TrialReminder.shouldRemind(true, start, start + 6, alreadyShown = false))
    }

    @Test
    fun `never reminds when no trial started`() {
        assertFalse(TrialReminder.shouldRemind(false, 0L, 6L, alreadyShown = false))
    }

    @Test
    fun `never reminds twice`() {
        assertFalse(TrialReminder.shouldRemind(false, start, start + 6, alreadyShown = true))
    }
}
