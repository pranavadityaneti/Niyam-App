package com.myniyam.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import com.myniyam.app.data.BlockList
import com.myniyam.app.data.UserPrefs
import com.myniyam.app.overlay.OverlayManager

class AppLockAccessibilityService : AccessibilityService() {

    @Volatile private var lastDismissedPkg: String? = null
    @Volatile private var lastDismissedAtMs: Long = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (!BlockList.matches(pkg)) {
            // Foreground moved to a non-blocked app — stop the interval timer and
            // drop the overlay if it's still up.
            IntervalCheckIn.onLeftBlocked()
            if (OverlayHideDecision.shouldHide(
                    overlayShowing = OverlayManager.isShowing(),
                    foregroundPkg = pkg,
                    foregroundClass = event.className?.toString(),
                    isBlocked = false,
                    ownPkg = packageName
                )
            ) {
                OverlayManager.hide(applicationContext)
            }
            return
        }

        // Opt-in interval check-in (SP-P-PAUSE): independent of grace — arm whenever
        // a blocked app is foreground so a long scroll still gets a pause.
        val snap = UserPrefs.snapshot()
        IntervalCheckIn.onBlockedForeground(
            pkg,
            enabled = snap.intervalCheckInEnabled,
            intervalMs = PauseConfig.intervalMillis(snap.intervalMinutes)
        ) { firedPkg -> fireBlock(firedPkg) }

        val now = SystemClock.elapsedRealtime()

        // Grace: a completed read (Continue) buys this package a re-block-free window.
        if (UnlockGrace.isActive(pkg, now)) return

        // Debounce: ignore re-triggers within 2 seconds of last dismissal for the same package.
        if (pkg == lastDismissedPkg && (now - lastDismissedAtMs) < DEBOUNCE_MS) return

        fireBlock(pkg)
    }

    /** Show the mantra overlay for [pkg] via the foreground service (launch + interval paths). */
    private fun fireBlock(pkg: String) {
        val intent = Intent(this, AppLockForegroundService::class.java).apply {
            action = AppLockForegroundService.ACTION_BLOCKED_APP_FOREGROUND
            putExtra(AppLockForegroundService.EXTRA_PACKAGE, pkg)
        }
        startService(intent)
    }

    override fun onInterrupt() = Unit

    fun markDismissed(pkg: String) {
        lastDismissedPkg = pkg
        lastDismissedAtMs = SystemClock.elapsedRealtime()
    }

    companion object {
        private const val DEBOUNCE_MS = 2_000L

        @Volatile private var instance: AppLockAccessibilityService? = null

        fun get(): AppLockAccessibilityService? = instance
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        instance = null
        IntervalCheckIn.onLeftBlocked()  // drop any pending interval timer
        super.onDestroy()
    }
}
