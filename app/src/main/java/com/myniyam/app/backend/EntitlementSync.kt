package com.myniyam.app.backend

import io.github.jan.supabase.functions.functions
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
