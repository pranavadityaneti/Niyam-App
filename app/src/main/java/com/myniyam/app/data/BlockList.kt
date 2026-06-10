package com.myniyam.app.data

object BlockList {

    /** First-run default + prefs-load fallback — the pre-SP3 hardcoded set. */
    val DEFAULT_PACKAGES: Set<String> = setOf(
        "com.instagram.android",
        "com.facebook.katana",
        "com.google.android.youtube"
    )

    fun matches(packageName: String): Boolean =
        packageName in UserPrefs.snapshot().blockedPackages
}
