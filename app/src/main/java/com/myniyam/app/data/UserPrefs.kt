package com.myniyam.app.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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

    data class Snapshot(
        val onboardingComplete: Boolean,
        val currentMantraId: String,
        val displayLanguage: DisplayLanguage,
        val blockedPackages: Set<String>
    ) {
        companion object {
            val DEFAULTS = Snapshot(
                onboardingComplete = false,
                currentMantraId = "gayatri",
                displayLanguage = DisplayLanguage.DEVANAGARI_SANSKRIT,
                blockedPackages = setOf(
                    "com.instagram.android",
                    "com.facebook.katana",
                    "com.google.android.youtube"
                )
            )

            fun fromRaw(
                onboardingComplete: Boolean?,
                mantraId: String?,
                language: String?,
                blocked: Set<String>?
            ): Snapshot = Snapshot(
                onboardingComplete = onboardingComplete ?: DEFAULTS.onboardingComplete,
                currentMantraId = mantraId?.takeIf { it.isNotBlank() } ?: DEFAULTS.currentMantraId,
                displayLanguage = language?.let { raw ->
                    DisplayLanguage.entries.firstOrNull { it.name == raw }
                } ?: DEFAULTS.displayLanguage,
                blockedPackages = blocked?.takeIf { it.isNotEmpty() } ?: DEFAULTS.blockedPackages
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
                    blocked = p[KEY_BLOCKED_PACKAGES]
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load prefs; using defaults", e)
                current = Snapshot.DEFAULTS
            }
        }
    }

    suspend fun setCurrentMantra(context: Context, mantraId: String) {
        context.niyamDataStore.edit { it[KEY_CURRENT_MANTRA_ID] = mantraId }
        current = current.copy(currentMantraId = mantraId)
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

    fun setSnapshotForTest(snapshot: Snapshot) { current = snapshot }

    fun resetForTest() { current = Snapshot.DEFAULTS; loadAttempted = false }
}
