package com.myniyam.app.data

object BlockList {

    val HARDCODED_PACKAGES: Set<String> = setOf(
        "com.instagram.android",
        "com.facebook.katana",
        "com.google.android.youtube"
    )

    fun matches(packageName: String): Boolean = packageName in HARDCODED_PACKAGES
}
