package com.myniyam.app.service

/**
 * Decides whether a window-state change to a non-blocked foreground should dismiss a showing
 * overlay. The overlay window itself reports our own package when it attaches — that event must
 * not self-dismiss, but the real app (MainActivity) coming to the front should.
 */
object OverlayHideDecision {

    private const val SYSTEM_UI_PKG = "com.android.systemui"
    private const val MAIN_ACTIVITY_CLASS = "com.myniyam.app.MainActivity"

    fun shouldHide(
        overlayShowing: Boolean,
        foregroundPkg: String,
        foregroundClass: String?,
        isBlocked: Boolean,
        ownPkg: String
    ): Boolean {
        if (!overlayShowing) return false
        if (isBlocked) return false
        if (foregroundPkg == SYSTEM_UI_PKG) return false
        if (foregroundPkg == ownPkg) return foregroundClass == MAIN_ACTIVITY_CLASS
        return true
    }
}
