package com.myniyam.app.data

/**
 * Single source of truth for the blockable-app shortlist shown in onboarding
 * (AppsScreen) and Settings (BlockedAppsSettingScreen). The engine blocks ANY
 * package the user selects — this list is just the curated, logo-bearing
 * shortlist; it is not a limit on what can be blocked.
 *
 * [logoSlug] keys an optional bundled brand drawable named `ic_app_<slug>`.
 * When that drawable is absent, AppIcon falls back to the installed app's
 * launcher icon, then to an initial-letter circle.
 */
data class CatalogApp(
    val name: String,
    val pkg: String,
    val logoSlug: String
)

object AppCatalog {
    val APPS: List<CatalogApp> = listOf(
        // Social / video
        CatalogApp("Instagram", "com.instagram.android", "instagram"),
        CatalogApp("YouTube", "com.google.android.youtube", "youtube"),
        CatalogApp("Facebook", "com.facebook.katana", "facebook"),
        CatalogApp("X (formerly Twitter)", "com.twitter.android", "x"),
        CatalogApp("Reddit", "com.reddit.frontpage", "reddit"),
        CatalogApp("Snapchat", "com.snapchat.android", "snapchat"),
        CatalogApp("TikTok", "com.zhiliaoapp.musically", "tiktok"),
        // Games
        CatalogApp("BGMI", "com.pubg.imobile", "bgmi"),
        CatalogApp("Free Fire", "com.dts.freefireth", "freefire"),
        CatalogApp("Call of Duty Mobile", "com.activision.callofduty.shooter", "codmobile"),
        CatalogApp("Candy Crush Saga", "com.king.candycrushsaga", "candycrush"),
        CatalogApp("Ludo King", "com.ludo.king", "ludoking")
    )
}
