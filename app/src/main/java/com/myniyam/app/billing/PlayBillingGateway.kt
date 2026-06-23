package com.myniyam.app.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.myniyam.app.backend.EntitlementSync
import com.myniyam.app.data.UserPrefs
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

/**
 * Real Google Play Billing (Library 7) implementation of [BillingGateway] (P7).
 * Subscriptions only. The purchase result arrives asynchronously through the
 * [PurchasesUpdatedListener], which we bridge to a coroutine via a per-flow
 * [CompletableDeferred]. Entitlement is persisted to local [UserPrefs] here;
 * P5c adds server-side verification on top. Never on the engine hot path.
 */
object PlayBillingGateway : BillingGateway {

    @Volatile private var client: BillingClient? = null
    // Resolved by the listener with the purchases from the most recent flow.
    @Volatile private var pending: CompletableDeferred<List<Purchase>?>? = null

    private val purchasesListener = PurchasesUpdatedListener { result, purchases ->
        val deferred = pending
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            deferred?.complete(purchases ?: emptyList())
        } else {
            // USER_CANCELED, ITEM_ALREADY_OWNED, errors → no new grant from this flow.
            deferred?.complete(null)
        }
    }

    private val purchaseMutex = kotlinx.coroutines.sync.Mutex()

    override suspend fun purchase(ctx: Context, plan: Plan): Boolean = purchaseMutex.withLock {
        // Serialized: only one purchase flow at a time, so the single shared
        // `pending` can never be resolved by another flow's result.
        val activity = ctx as? Activity ?: return@withLock false
        val c = ensureConnected(ctx) ?: return@withLock false
        val details = queryDetails(c, plan.productId) ?: return@withLock false
        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return@withLock false

        val deferred = CompletableDeferred<List<Purchase>?>()
        pending = deferred
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .setOfferToken(offerToken)
                        .build()
                )
            )
            .build()

        val launch = c.launchBillingFlow(activity, flowParams)
        if (launch.responseCode != BillingClient.BillingResponseCode.OK) {
            pending = null
            return@withLock false
        }

        // Bounded wait: if Play never calls the listener (process edge cases),
        // don't hang the caller forever.
        val purchases = try {
            kotlinx.coroutines.withTimeoutOrNull(5 * 60_000L) { deferred.await() }
        } finally {
            pending = null
        }
        if (purchases.isNullOrEmpty()) return@withLock false

        var granted = false
        for (p in purchases) {
            if (p.purchaseState == Purchase.PurchaseState.PURCHASED) {
                persistAndAcknowledge(ctx, c, p)
                granted = true
            }
        }
        granted
    }

    override suspend fun restorePurchases(ctx: Context): Boolean {
        val c = ensureConnected(ctx) ?: return false
        val result = c.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) return false

        var restored = false
        for (p in result.purchasesList) {
            if (p.purchaseState == Purchase.PurchaseState.PURCHASED) {
                persistAndAcknowledge(ctx, c, p)
                restored = true
            }
        }
        return restored
    }

    override fun currentPlan(): Plan? =
        UserPrefs.snapshot().premiumPlan?.let { p -> Plan.entries.firstOrNull { it.name == p } }

    // --- internals -----------------------------------------------------------

    private suspend fun ensureConnected(ctx: Context): BillingClient? {
        client?.let { if (it.isReady) return it }
        val c = client ?: BillingClient.newBuilder(ctx.applicationContext)
            .setListener(purchasesListener)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
            )
            .build()
            .also { client = it }

        return suspendCancellableCoroutine { cont ->
            c.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (cont.isActive) {
                        cont.resume(
                            if (result.responseCode == BillingClient.BillingResponseCode.OK) c else null
                        )
                    }
                }

                override fun onBillingServiceDisconnected() {
                    if (cont.isActive) cont.resume(null)
                }
            })
        }
    }

    private suspend fun queryDetails(c: BillingClient, productId: String): ProductDetails? {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()
        val result = c.queryProductDetails(params)
        return result.productDetailsList?.firstOrNull()
    }

    /** Grant locally and acknowledge with Play (required within 3 days or Play refunds). */
    private suspend fun persistAndAcknowledge(ctx: Context, c: BillingClient, p: Purchase) {
        val planName = p.products.firstNotNullOfOrNull { Plan.fromProductId(it)?.name }
        if (planName != null) UserPrefs.setPremium(ctx, planName)
        if (!p.isAcknowledged) {
            c.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(p.purchaseToken)
                    .build()
            )
        }
        // Record the trusted entitlement server-side (P5c). Best-effort: a failure
        // here does not undo the local grant above — launch reconcile (5c-3) is
        // where the server's verdict becomes authoritative.
        val productId = p.products.firstOrNull()
        if (productId != null) {
            EntitlementSync.verifyPurchase(productId, p.purchaseToken)
        }
    }
}
