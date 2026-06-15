package com.myniyam.app.backend

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
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

    suspend fun signOut() {
        auth.signOut()
    }
}
