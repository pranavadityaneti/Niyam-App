package com.myniyam.app.billing

import com.myniyam.app.BuildConfig
import com.myniyam.app.data.UserPrefs

/**
 * Subscription plans (spec §3.2). [priceInr] is the displayed sandbox price;
 * real prices come from Play once live. [productId] is the Play Console
 * subscription product id (P7). Repriceable in one place.
 */
enum class Plan(val priceInr: Int, val periodKey: String, val productId: String) {
    WEEKLY(15, "week", "niyam.premium.weekly"),
    MONTHLY(49, "month", "niyam.premium.monthly"),
    YEARLY(399, "year", "niyam.premium.yearly");

    companion object {
        fun fromProductId(productId: String): Plan? = entries.firstOrNull { it.productId == productId }
    }
}

/**
 * Abstraction over the purchase flow (spec §3.2). Sandbox for local/debug,
 * real Play Billing for release (P7) — selected by [Billing.gateway]; no UI change.
 */
interface BillingGateway {
    /** Launch the purchase flow for [plan]. Returns true once the purchase is owned. */
    suspend fun purchase(ctx: android.content.Context, plan: Plan): Boolean
    /** Re-grant entitlement from any existing owned purchase. Returns true if one was found. */
    suspend fun restorePurchases(ctx: android.content.Context): Boolean
    fun currentPlan(): Plan?
}

/**
 * Active gateway for this build: Sandbox in debug (so the paywall is testable
 * on an emulator with no Play account), real Play Billing in release (where
 * closed-test / production purchases run).
 */
object Billing {
    val gateway: BillingGateway = if (BuildConfig.DEBUG) SandboxBillingGateway else PlayBillingGateway
}

/** Sandbox impl: instant success, persisted via [UserPrefs]. No real payment. */
object SandboxBillingGateway : BillingGateway {
    override suspend fun purchase(ctx: android.content.Context, plan: Plan): Boolean {
        UserPrefs.setPremium(ctx, plan.name)
        return true
    }

    override suspend fun restorePurchases(ctx: android.content.Context): Boolean =
        UserPrefs.snapshot().premiumActive

    override fun currentPlan(): Plan? =
        UserPrefs.snapshot().premiumPlan?.let { p -> Plan.entries.firstOrNull { it.name == p } }
}
