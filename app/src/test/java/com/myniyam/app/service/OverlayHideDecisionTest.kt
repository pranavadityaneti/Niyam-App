package com.myniyam.app.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayHideDecisionTest {

    private val ownPkg = "com.myniyam.app"

    @Test
    fun `never hide when overlay is not showing`() {
        assertFalse(
            OverlayHideDecision.shouldHide(
                overlayShowing = false,
                foregroundPkg = "com.android.launcher3",
                foregroundClass = null,
                isBlocked = false,
                ownPkg = ownPkg
            )
        )
    }

    @Test
    fun `never hide while a blocked app is foreground`() {
        assertFalse(
            OverlayHideDecision.shouldHide(
                overlayShowing = true,
                foregroundPkg = "com.google.android.youtube",
                foregroundClass = "com.google.android.youtube.HomeActivity",
                isBlocked = true,
                ownPkg = ownPkg
            )
        )
    }

    @Test
    fun `system ui events do not hide`() {
        assertFalse(
            OverlayHideDecision.shouldHide(
                overlayShowing = true,
                foregroundPkg = "com.android.systemui",
                foregroundClass = null,
                isBlocked = false,
                ownPkg = ownPkg
            )
        )
    }

    @Test
    fun `our own overlay window attaching does not self-dismiss`() {
        assertFalse(
            OverlayHideDecision.shouldHide(
                overlayShowing = true,
                foregroundPkg = ownPkg,
                foregroundClass = "android.widget.FrameLayout",
                isBlocked = false,
                ownPkg = ownPkg
            )
        )
    }

    @Test
    fun `our own overlay window with null class does not self-dismiss`() {
        assertFalse(
            OverlayHideDecision.shouldHide(
                overlayShowing = true,
                foregroundPkg = ownPkg,
                foregroundClass = null,
                isBlocked = false,
                ownPkg = ownPkg
            )
        )
    }

    @Test
    fun `opening Niyam itself hides the overlay`() {
        assertTrue(
            OverlayHideDecision.shouldHide(
                overlayShowing = true,
                foregroundPkg = ownPkg,
                foregroundClass = "com.myniyam.app.MainActivity",
                isBlocked = false,
                ownPkg = ownPkg
            )
        )
    }

    @Test
    fun `going home hides the overlay`() {
        assertTrue(
            OverlayHideDecision.shouldHide(
                overlayShowing = true,
                foregroundPkg = "com.google.android.apps.nexuslauncher",
                foregroundClass = "com.google.android.apps.nexuslauncher.NexusLauncherActivity",
                isBlocked = false,
                ownPkg = ownPkg
            )
        )
    }

    @Test
    fun `switching to another non-blocked app hides the overlay`() {
        assertTrue(
            OverlayHideDecision.shouldHide(
                overlayShowing = true,
                foregroundPkg = "com.whatsapp",
                foregroundClass = "com.whatsapp.HomeActivity",
                isBlocked = false,
                ownPkg = ownPkg
            )
        )
    }
}
