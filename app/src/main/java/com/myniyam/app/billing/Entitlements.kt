package com.myniyam.app.billing

import com.myniyam.app.data.DisplayLanguage

/** Premium tier the user is currently in (spec §3.1). */
enum class PremiumState { PREMIUM, TRIAL, FREE }

/**
 * Pure entitlement logic (spec §3.1) — no android imports, fully unit-testable.
 * Callers pass the clock (epoch days) so trial math survives wall-clock changes.
 */
object Entitlements {

    /** First-priority mantra per intention, free forever (spec §1). */
    val FREE_MANTRA_IDS: Set<String> = setOf(
        "gita-2-47",
        "mahamrityunjaya",
        "gayatri",
        "gita-4-7-8",
        "hanuman-chalisa-opening"
    )

    /** Display languages available without premium (spec §1). */
    val FREE_LANGUAGES: Set<DisplayLanguage> = setOf(
        DisplayLanguage.ENGLISH,
        DisplayLanguage.HINDI
    )

    const val TRIAL_DAYS = 7

    /**
     * PREMIUM if purchased; TRIAL if within [TRIAL_DAYS] of a started trial
     * (exclusive boundary, and only when today is not before the start so a
     * clock rollback can't grant an infinite trial); otherwise FREE.
     */
    fun state(premiumActive: Boolean, trialStartEpochDay: Long, todayEpochDay: Long): PremiumState {
        if (premiumActive) return PremiumState.PREMIUM
        val started = trialStartEpochDay != 0L
        val withinWindow = todayEpochDay >= trialStartEpochDay &&
            todayEpochDay - trialStartEpochDay < TRIAL_DAYS
        return if (started && withinWindow) PremiumState.TRIAL else PremiumState.FREE
    }

    fun isPremiumExperience(state: PremiumState): Boolean = state != PremiumState.FREE

    /** Grandfather rule: the current sadhana mantra always stays usable (spec §2). */
    fun canUseMantra(state: PremiumState, mantraId: String, currentMantraId: String): Boolean =
        isPremiumExperience(state) || mantraId in FREE_MANTRA_IDS || mantraId == currentMantraId

    /** Grandfather rule: the current display language always stays usable (spec §2). */
    fun canUseLanguage(state: PremiumState, lang: DisplayLanguage, currentLang: DisplayLanguage): Boolean =
        isPremiumExperience(state) || lang in FREE_LANGUAGES || lang == currentLang

    /** Whole days remaining in the trial; 0 when unset, elapsed, or after a clock rollback. */
    fun trialDaysLeft(trialStartEpochDay: Long, todayEpochDay: Long): Int {
        if (trialStartEpochDay == 0L) return 0
        if (todayEpochDay < trialStartEpochDay) return 0
        val elapsed = todayEpochDay - trialStartEpochDay
        if (elapsed >= TRIAL_DAYS) return 0
        return (TRIAL_DAYS - elapsed).toInt()
    }
}
