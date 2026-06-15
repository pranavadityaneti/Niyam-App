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
    /** Build default. RemoteConfig may replace [APPS] at launch (OTA). */
    val DEFAULT_APPS: List<CatalogApp> = listOf(
        // Social / video
        CatalogApp("Instagram", "com.instagram.android", "instagram"),
        CatalogApp("YouTube", "com.google.android.youtube", "youtube"),
        CatalogApp("Facebook", "com.facebook.katana", "facebook"),
        CatalogApp("X (formerly Twitter)", "com.twitter.android", "x"),
        CatalogApp("Reddit", "com.reddit.frontpage", "reddit"),
        CatalogApp("Snapchat", "com.snapchat.android", "snapchat"),
        CatalogApp("TikTok", "com.zhiliaoapp.musically", "tiktok"),
        // Games
        CatalogApp("Free Fire", "com.dts.freefireth", "freefire"),
        CatalogApp("Call of Duty Mobile", "com.activision.callofduty.shooter", "codmobile"),
        CatalogApp("Candy Crush Saga", "com.king.candycrushsaga", "candycrush"),
        CatalogApp("Ludo King", "com.ludo.king", "ludoking")
    )

    /**
     * The catalog actually shown in the picker. Defaults to [DEFAULT_APPS];
     * RemoteConfig overwrites it on launch so you can add a blockable app
     * without a build. Only changes what's OFFERED — the engine still blocks
     * any package the user has selected.
     */
    @Volatile
    var APPS: List<CatalogApp> = DEFAULT_APPS
}
