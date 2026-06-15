package com.myniyam.app.backend

import android.content.Context
import com.myniyam.app.data.UserPrefs
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Syncs the user's practice (current sadhana, journey, intention, language) and
 * favourites with Supabase (P5). Model: the device is the working copy (engine
 * + UI read local UserPrefs); the server is a backup that also seeds a fresh
 * device for a returning user. Push on app stop; seed-from-server at sign-in.
 * All calls are best-effort — failures never disturb local state, and none of
 * this is on the blocking-engine path.
 */
object PracticeSync {

    @Serializable
    private data class PracticeRow(
        @SerialName("user_id") val userId: String? = null,
        @SerialName("current_mantra_id") val currentMantraId: String? = null,
        @SerialName("sadhana_start_epoch_day") val sadhanaStartEpochDay: Long? = null,
        @SerialName("completed_mantra_ids") val completedMantraIds: List<String> = emptyList(),
        @SerialName("selected_intention") val selectedIntention: String? = null,
        @SerialName("display_language") val displayLanguage: String? = null,
        @SerialName("streak_count") val streakCount: Int = 0,
    )

    @Serializable
    private data class FavouriteRow(
        @SerialName("user_id") val userId: String,
        @SerialName("mantra_id") val mantraId: String,
    )

    /** Push local practice + favourites to the server. Best-effort; no-op when signed out. */
    suspend fun push(context: Context) {
        val uid = AuthRepository.currentUserId() ?: return
        val s = UserPrefs.snapshot()
        try {
            val client = SupabaseClientProvider.client
            client.from("practice_state").upsert(
                PracticeRow(
                    userId = uid,
                    currentMantraId = s.currentMantraId,
                    sadhanaStartEpochDay = s.sadhanaStartEpochDay,
                    completedMantraIds = s.completedMantraIds.toList(),
                    selectedIntention = s.selectedIntention.name,
                    displayLanguage = s.displayLanguage.name,
                    // streak is derived from the local read-events DB (not synced);
                    // kept device-local for now — see forlater (streak sync).
                    streakCount = 0,
                )
            ) { onConflict = "user_id" }

            // Replace the user's favourites with the current local set (RLS scopes
            // delete to own rows).
            client.from("favourites").delete { filter { eq("user_id", uid) } }
            if (s.favouriteMantraIds.isNotEmpty()) {
                client.from("favourites").insert(s.favouriteMantraIds.map { FavouriteRow(uid, it) })
            }
        } catch (e: Exception) {
            // best-effort
        }
    }

    /**
     * If the server holds practice for this user, seed it into local prefs and
     * return true (a returning user on a fresh device skips onboarding). Returns
     * false when signed out, on error, or when the server has no practice row.
     */
    suspend fun seedFromServerIfPresent(context: Context): Boolean {
        if (!AuthRepository.isSignedIn()) return false
        return try {
            val client = SupabaseClientProvider.client
            val row = client.from("practice_state").select().decodeList<PracticeRow>().firstOrNull()
                ?: return false
            val favs = client.from("favourites").select()
                .decodeList<FavouriteRow>().map { it.mantraId }.toSet()
            UserPrefs.applyServerPractice(
                context,
                currentMantraId = row.currentMantraId,
                sadhanaStartEpochDay = row.sadhanaStartEpochDay ?: 0L,
                completedMantraIds = row.completedMantraIds.toSet(),
                intention = row.selectedIntention,
                displayLanguage = row.displayLanguage,
                favourites = favs,
            )
            true
        } catch (e: Exception) {
            false
        }
    }
}
