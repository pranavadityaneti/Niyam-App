package com.myniyam.app.backend

import android.content.Context
import com.myniyam.app.billing.Plan
import com.myniyam.app.data.UserPrefs
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Talks to the `verify-entitlement` Edge Function (P5c). Sends a Play purchase
 * token to the server, which verifies it with Google and writes the trusted
 * `entitlements` row. Never on the engine hot path; all calls are best-effort
 * and a failure NEVER downgrades a locally-valid entitlement (see callers).
 */
object EntitlementSync {

    @Serializable
    private data class VerifyRequest(val productId: String, val purchaseToken: String)

    @Serializable
    private data class VerifyResponse(
        @SerialName("premium_active") val premiumActive: Boolean = false,
        @SerialName("premium_plan") val premiumPlan: String? = null,
    )

    @Serializable
    private data class EntitlementRow(
        @SerialName("premium_active") val premiumActive: Boolean = false,
        @SerialName("premium_plan") val premiumPlan: String? = null,
        @SerialName("trial_start_epoch_day") val trialStartEpochDay: Long? = null,
        @SerialName("source") val source: String? = null,
    )

    @Serializable
    private data class TrialRequest(val trialStartEpochDay: Long)

    @Serializable
    private data class TrialResponse(
        @SerialName("trial_start_epoch_day") val trialStartEpochDay: Long = 0L,
    )

    /**
     * Report this device's trial start to the server (P5c-4). The server keeps
     * the earliest start, so a reinstall can't push the trial later. Returns the
     * effective server start, or null on failure. Best-effort.
     */
    suspend fun syncTrial(trialStartEpochDay: Long): Long? {
        return try {
            val response = SupabaseClientProvider.client.functions.invoke("sync-trial") {
                setBody(TrialRequest(trialStartEpochDay))
            }
            response.body<TrialResponse>().trialStartEpochDay
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Read the server-trusted entitlements row on app launch and mirror it into
     * local [UserPrefs] (P5c). This restores a paying user's premium on a new
     * device with no Play action, and revokes locally when the server says the
     * subscription is no longer active. No-op when signed out. Any failure
     * (offline, no row) leaves local state untouched — never locks out a paying
     * user on a flaky connection.
     */
    suspend fun reconcileOnLaunch(context: Context) {
        if (!AuthRepository.isSignedIn()) return
        try {
            val row = SupabaseClientProvider.client
                .from("entitlements")
                .select()
                .decodeList<EntitlementRow>()
                .firstOrNull()

            // Premium: grant whenever the server says active; only REVOKE on an
            // authoritative Play-sourced "inactive". A trial-sourced row (written
            // by sync-trial) carries premium_active=false by column default and
            // must NOT be read as "subscription inactive" — otherwise a paid user
            // whose purchase verify hasn't landed yet gets wrongly downgraded.
            if (row != null) {
                val planName = row.premiumPlan?.let { Plan.fromProductId(it)?.name ?: it }
                when {
                    row.premiumActive -> UserPrefs.setPremiumActive(context, true, planName)
                    row.source == "play" -> UserPrefs.setPremiumActive(context, false, null)
                    // non-Play row (trial/unknown) with premium false → leave local premium untouched
                }
            }

            // Trial: earliest-start-wins, kept in sync both directions.
            val serverTrial = row?.trialStartEpochDay ?: 0L
            val localTrial = UserPrefs.snapshot().trialStartEpochDay
            when {
                // Server has an earlier (or the only) start → adopt it locally so a
                // reinstall can't restart the trial.
                serverTrial > 0L && (localTrial == 0L || serverTrial < localTrial) ->
                    UserPrefs.startTrial(context, serverTrial)
                // Server doesn't know about our trial yet → report it.
                localTrial > 0L && serverTrial == 0L ->
                    syncTrial(localTrial)
            }
        } catch (e: Exception) {
            // Keep current local state.
        }
    }

    /**
     * Ask the server to verify a Play purchase and record entitlement.
     * Returns the server's verdict (true/false), or null if the call could not
     * be completed (offline, not signed in, function error) — callers treat
     * null as "unknown, keep current state".
     */
    suspend fun verifyPurchase(productId: String, purchaseToken: String): Boolean? {
        return try {
            val response = SupabaseClientProvider.client.functions.invoke("verify-entitlement") {
                setBody(VerifyRequest(productId, purchaseToken))
            }
            response.body<VerifyResponse>().premiumActive
        } catch (e: Exception) {
            null
        }
    }
}
