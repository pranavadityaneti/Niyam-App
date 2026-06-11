package com.myniyam.app.billing

/**
 * Pure decision for the day-6 "trial ends tomorrow" reminder (SP-12 spec §2).
 * True only while the trial is genuinely in its final day and the reminder
 * hasn't fired before. Premium users and never-started trials never remind.
 */
object TrialReminder {

    fun shouldRemind(
        premiumActive: Boolean,
        trialStartEpochDay: Long,
        todayEpochDay: Long,
        alreadyShown: Boolean
    ): Boolean {
        if (alreadyShown) return false
        val state = Entitlements.state(premiumActive, trialStartEpochDay, todayEpochDay)
        if (state != PremiumState.TRIAL) return false
        return Entitlements.trialDaysLeft(trialStartEpochDay, todayEpochDay) <= 1
    }
}
