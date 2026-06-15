package com.myniyam.app.backend

import com.myniyam.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Lazy Supabase client singleton (SP-P2 foundation). Auth + Postgrest only.
 * The anon key + URL are client-safe (RLS-protected); the service_role key is
 * never present in the app. The client is created lazily and is NEVER on the
 * blocking-engine hot path — the engine reads/writes local UserPrefs only.
 */
object SupabaseClientProvider {

    /** True when build config carries real values (false on CI builds without props). */
    fun isConfigured(): Boolean =
        BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
        }
    }
}
