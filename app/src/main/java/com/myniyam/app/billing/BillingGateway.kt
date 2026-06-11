package com.myniyam.app.billing

import com.myniyam.app.data.UserPrefs

/** Subscription plans and sandbox pricing in INR (spec §3.2). Repriceable in one place. */
enum class Plan(val priceInr: Int, val periodKey: String) {
    WEEKLY(15, "week"),
    MONTHLY(49, "month"),
    YEARLY(399, "year")
}

/**
 * Abstraction over the purchase flow (spec §3.2). Swapping in RevenueCat /
 * Play Billing later = one new implementation behind this interface + real
 * product ids; no UI change.
 */
interface BillingGateway {
    suspend fun purchase(ctx: android.content.Context, plan: Plan): Boolean
    fun currentPlan(): Plan?
}

/** Sandbox impl: instant success, persisted via [UserPrefs]. No real payment. */
object SandboxBillingGateway : BillingGateway {
    override suspend fun purchase(ctx: android.content.Context, plan: Plan): Boolean {
        UserPrefs.setPremium(ctx, plan.name)
        return true
    }

    override fun currentPlan(): Plan? =
        UserPrefs.snapshot().premiumPlan?.let { p -> Plan.entries.firstOrNull { it.name == p } }
}
