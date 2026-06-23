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

            // Diff-based favourites sync — never a zero-rows window (the old
            // delete-all-then-insert could lose every favourite on a partial
            // failure). Add the new ones, remove only the dropped ones.
            val current = s.favouriteMantraIds
            val serverFavs = client.from("favourites").select()
                .decodeList<FavouriteRow>().map { it.mantraId }.toSet()
            val toAdd = current - serverFavs
            val toRemove = serverFavs - current
            if (toAdd.isNotEmpty()) {
                client.from("favourites").insert(toAdd.map { FavouriteRow(uid, it) })
            }
            toRemove.forEach { mid ->
                client.from("favourites").delete {
                    filter { eq("user_id", uid); eq("mantra_id", mid) }
                }
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
        // Only seed onto a FRESH device. An already-onboarded device holds the
        // user's live (possibly newer) practice — never overwrite it with the
        // server copy on sign-in, or a sign-out/in cycle could clobber local edits.
        if (UserPrefs.snapshot().onboardingComplete) return false
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
