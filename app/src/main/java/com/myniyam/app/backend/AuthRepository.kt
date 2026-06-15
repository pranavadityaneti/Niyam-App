package com.myniyam.app.backend

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.functions.functions
import kotlinx.coroutines.flow.StateFlow

/**
 * Thin wrapper over Supabase Auth (SP-P3). Session is persisted by supabase-kt;
 * NONE of this is on the blocking-engine path — the engine reads local UserPrefs.
 */
object AuthRepository {

    private val auth get() = SupabaseClientProvider.client.auth

    /** Observable auth state (Authenticated / NotAuthenticated / loading). */
    val sessionStatus: StateFlow<SessionStatus> get() = auth.sessionStatus

    fun isSignedIn(): Boolean = auth.currentSessionOrNull() != null

    fun currentEmail(): String? = auth.currentUserOrNull()?.email

    fun currentUserId(): String? = auth.currentUserOrNull()?.id

    suspend fun signOut() {
        auth.signOut()
    }

    /**
     * Permanently delete the signed-in user's server account (P3b). Invokes the
     * `delete-account` Edge Function, which verifies the caller's JWT and uses
     * the service_role key (server-only) to delete the auth user; the DB tables
     * cascade-delete via their FK to auth.users. The function call carries the
     * session bearer token automatically. After a successful return the caller
     * must wipe local state and sign out. Throws on failure (caller handles).
     */
    suspend fun deleteAccount() {
        SupabaseClientProvider.client.functions.invoke("delete-account")
        auth.signOut()
    }
}
