package com.myniyam.app.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.niyamDataStore by preferencesDataStore(name = "niyam_user_prefs")

/**
 * User choices persisted via DataStore, exposed to engine code as a
 * synchronous @Volatile snapshot (spec §7). Warm-up: ensureLoaded() at
 * Application start, same pattern as MantraRepository.
 */
object UserPrefs {

    private const val TAG = "UserPrefs"

    private val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    private val KEY_CURRENT_MANTRA_ID = stringPreferencesKey("current_mantra_id")
    private val KEY_DISPLAY_LANGUAGE = stringPreferencesKey("display_language")
    private val KEY_BLOCKED_PACKAGES = stringSetPreferencesKey("blocked_packages")
    private val KEY_SELECTED_INTENTION = stringPreferencesKey("selected_intention")
    private val KEY_SADHANA_START = longPreferencesKey("sadhana_start_epoch_day")
    private val KEY_COMPLETED_MANTRAS = stringSetPreferencesKey("completed_mantra_ids")
    private val KEY_PENDING_CELEBRATION = booleanPreferencesKey("pending_celebration")
    private val KEY_THEME_PREF = stringPreferencesKey("theme_pref")
    private val KEY_NOTIFY_ON_COMPLETION = booleanPreferencesKey("notify_on_completion")
    private val KEY_TRIAL_START = longPreferencesKey("trial_start_epoch_day")
    private val KEY_PREMIUM_ACTIVE = booleanPreferencesKey("premium_active")
    private val KEY_PREMIUM_PLAN = stringPreferencesKey("premium_plan")
    private val KEY_TRIAL_REMINDER_SHOWN = booleanPreferencesKey("trial_reminder_shown")

    data class Snapshot(
        val onboardingComplete: Boolean,
        val currentMantraId: String,
        val displayLanguage: DisplayLanguage,
        val blockedPackages: Set<String>,
        val selectedIntention: Intention,
        val sadhanaStartEpochDay: Long,
        val completedMantraIds: Set<String>,
        val pendingCelebration: Boolean,
        val themePref: ThemePref,
        val notifyOnCompletion: Boolean,
        val trialStartEpochDay: Long,
        val premiumActive: Boolean,
        val premiumPlan: String?,
        val trialReminderShown: Boolean
    ) {
        companion object {
            val DEFAULTS = Snapshot(
                onboardingComplete = false,
                currentMantraId = "gayatri",
                displayLanguage = DisplayLanguage.DEVANAGARI_SANSKRIT,
                blockedPackages = BlockList.DEFAULT_PACKAGES,
                selectedIntention = Intention.SADHANA,
                sadhanaStartEpochDay = 0L,
                completedMantraIds = emptySet(),
                pendingCelebration = false,
                themePref = ThemePref.LIGHT,
                notifyOnCompletion = true,
                trialStartEpochDay = 0L,
                premiumActive = false,
                premiumPlan = null,
                trialReminderShown = false
            )

            fun fromRaw(
                onboardingComplete: Boolean?,
                mantraId: String?,
                language: String?,
                blocked: Set<String>?,
                intention: String? = null,
                sadhanaStart: Long? = null,
                completed: Set<String>? = null,
                pendingCelebration: Boolean? = null,
                themePref: String? = null,
                notifyOnCompletion: Boolean? = null,
                trialStart: Long? = null,
                premiumActive: Boolean? = null,
                premiumPlan: String? = null,
                trialReminderShown: Boolean? = null
            ): Snapshot = Snapshot(
                onboardingComplete = onboardingComplete ?: DEFAULTS.onboardingComplete,
                currentMantraId = mantraId?.takeIf { it.isNotBlank() } ?: DEFAULTS.currentMantraId,
                displayLanguage = language?.let { raw ->
                    DisplayLanguage.entries.firstOrNull { it.name == raw }
                } ?: DEFAULTS.displayLanguage,
                blockedPackages = blocked?.takeIf { it.isNotEmpty() } ?: DEFAULTS.blockedPackages,
                selectedIntention = intention?.let { raw ->
                    Intention.entries.firstOrNull { it.name == raw }
                } ?: DEFAULTS.selectedIntention,
                sadhanaStartEpochDay = sadhanaStart ?: DEFAULTS.sadhanaStartEpochDay,
                completedMantraIds = completed ?: DEFAULTS.completedMantraIds,
                pendingCelebration = pendingCelebration ?: DEFAULTS.pendingCelebration,
                themePref = themePref?.let { raw ->
                    ThemePref.entries.firstOrNull { it.name == raw }
                } ?: DEFAULTS.themePref,
                notifyOnCompletion = notifyOnCompletion ?: DEFAULTS.notifyOnCompletion,
                trialStartEpochDay = trialStart ?: DEFAULTS.trialStartEpochDay,
                premiumActive = premiumActive ?: DEFAULTS.premiumActive,
                premiumPlan = premiumPlan ?: DEFAULTS.premiumPlan,
                trialReminderShown = trialReminderShown ?: DEFAULTS.trialReminderShown
            )
        }
    }

    @Volatile private var current: Snapshot = Snapshot.DEFAULTS
    @Volatile private var loadAttempted = false

    fun snapshot(): Snapshot = current

    /** Blocking read of persisted prefs into the snapshot. Call from the warm-up thread. */
    fun ensureLoaded(context: Context) {
        if (loadAttempted) return
        synchronized(this) {
            if (loadAttempted) return
            loadAttempted = true
            try {
                val p = runBlocking { context.niyamDataStore.data.first() }
                current = Snapshot.fromRaw(
                    onboardingComplete = p[KEY_ONBOARDING_COMPLETE],
                    mantraId = p[KEY_CURRENT_MANTRA_ID],
                    language = p[KEY_DISPLAY_LANGUAGE],
                    blocked = p[KEY_BLOCKED_PACKAGES],
                    intention = p[KEY_SELECTED_INTENTION],
                    sadhanaStart = p[KEY_SADHANA_START],
                    completed = p[KEY_COMPLETED_MANTRAS],
                    pendingCelebration = p[KEY_PENDING_CELEBRATION],
                    themePref = p[KEY_THEME_PREF],
                    notifyOnCompletion = p[KEY_NOTIFY_ON_COMPLETION],
                    trialStart = p[KEY_TRIAL_START],
                    premiumActive = p[KEY_PREMIUM_ACTIVE],
                    premiumPlan = p[KEY_PREMIUM_PLAN],
                    trialReminderShown = p[KEY_TRIAL_REMINDER_SHOWN]
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load prefs; using defaults", e)
                current = Snapshot.DEFAULTS
            }
        }
    }

    suspend fun setCurrentMantra(context: Context, mantraId: String, startEpochDay: Long = java.time.LocalDate.now().toEpochDay()) {
        context.niyamDataStore.edit {
            it[KEY_CURRENT_MANTRA_ID] = mantraId
            it[KEY_SADHANA_START] = startEpochDay
            it[KEY_PENDING_CELEBRATION] = false
        }
        current = current.copy(currentMantraId = mantraId, sadhanaStartEpochDay = startEpochDay, pendingCelebration = false)
    }

    suspend fun setIntention(context: Context, intention: Intention) {
        context.niyamDataStore.edit { it[KEY_SELECTED_INTENTION] = intention.name }
        current = current.copy(selectedIntention = intention)
    }

    suspend fun setThemePref(context: Context, pref: ThemePref) {
        context.niyamDataStore.edit { it[KEY_THEME_PREF] = pref.name }
        current = current.copy(themePref = pref)
        com.myniyam.app.ui.theme.ThemeState.set(pref)
    }

    suspend fun setNotifyOnCompletion(context: Context, enabled: Boolean) {
        context.niyamDataStore.edit { it[KEY_NOTIFY_ON_COMPLETION] = enabled }
        current = current.copy(notifyOnCompletion = enabled)
    }

    suspend fun markCompleted(context: Context, mantraId: String) {
        val newSet = current.completedMantraIds + mantraId
        context.niyamDataStore.edit {
            it[KEY_COMPLETED_MANTRAS] = newSet
            it[KEY_PENDING_CELEBRATION] = true
        }
        current = current.copy(completedMantraIds = newSet, pendingCelebration = true)
    }

    suspend fun setDisplayLanguage(context: Context, language: DisplayLanguage) {
        context.niyamDataStore.edit { it[KEY_DISPLAY_LANGUAGE] = language.name }
        current = current.copy(displayLanguage = language)
    }

    suspend fun setBlockedPackages(context: Context, packages: Set<String>) {
        context.niyamDataStore.edit { it[KEY_BLOCKED_PACKAGES] = packages }
        current = current.copy(blockedPackages = packages)
    }

    suspend fun setOnboardingComplete(context: Context) {
        context.niyamDataStore.edit { it[KEY_ONBOARDING_COMPLETE] = true }
        current = current.copy(onboardingComplete = true)
    }

    suspend fun startTrial(context: Context, epochDay: Long) {
        context.niyamDataStore.edit { it[KEY_TRIAL_START] = epochDay }
        current = current.copy(trialStartEpochDay = epochDay)
    }

    suspend fun setPremium(context: Context, plan: String) {
        context.niyamDataStore.edit {
            it[KEY_PREMIUM_ACTIVE] = true
            it[KEY_PREMIUM_PLAN] = plan
        }
        current = current.copy(premiumActive = true, premiumPlan = plan)
    }

    /** Debug "show me the free tier" lever: drops premium AND resets the trial. */
    suspend fun clearPremiumForSandbox(context: Context) {
        context.niyamDataStore.edit {
            it[KEY_PREMIUM_ACTIVE] = false
            it.remove(KEY_PREMIUM_PLAN)
            it[KEY_TRIAL_START] = 0L
        }
        current = current.copy(premiumActive = false, premiumPlan = null, trialStartEpochDay = 0L)
    }

    /** Debug lever: backdate the trial start so state() computes FREE without touching premium. */
    suspend fun expireTrialForSandbox(context: Context, todayEpochDay: Long) {
        val backdated = todayEpochDay - 7
        context.niyamDataStore.edit { it[KEY_TRIAL_START] = backdated }
        current = current.copy(trialStartEpochDay = backdated)
    }

    suspend fun setTrialReminderShown(context: Context) {
        context.niyamDataStore.edit { it[KEY_TRIAL_REMINDER_SHOWN] = true }
        current = current.copy(trialReminderShown = true)
    }

    fun setSnapshotForTest(snapshot: Snapshot) { current = snapshot }

    fun resetForTest() { current = Snapshot.DEFAULTS; loadAttempted = false }
}

/** Appearance preference (spec §2). Default LIGHT (Pranav's ruling). */
enum class ThemePref { LIGHT, DARK, SYSTEM }
