package com.myniyam.app.billing

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.myniyam.app.backend.RemoteConfig

/**
 * Bottom-of-screen banner shown to FREE-tier users only (spec §3.6).
 *
 * The application id (manifest) and the [AD_UNIT_ID] below are Google's OFFICIAL
 * public AdMob test ids — no AdMob account is required to render them, and they
 * never bill anyone. When the real AdMob account exists this is a one-line swap
 * (here + the manifest `APPLICATION_ID` meta-data).
 *
 * Caller is responsible for the FREE-state gate; this composable just renders.
 */
private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    // OTA kill-switch: ads can be turned off remotely (default on).
    if (!RemoteConfig.flag("ads_enabled", true)) return
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = AD_UNIT_ID
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}
