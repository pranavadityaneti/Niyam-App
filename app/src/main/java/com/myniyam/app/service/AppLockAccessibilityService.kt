package com.myniyam.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import com.myniyam.app.data.BlockList

class AppLockAccessibilityService : AccessibilityService() {

    @Volatile private var lastDismissedPkg: String? = null
    @Volatile private var lastDismissedAtMs: Long = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (!BlockList.matches(pkg)) return

        // Debounce: ignore re-triggers within 2 seconds of last dismissal for the same package.
        val now = SystemClock.elapsedRealtime()
        if (pkg == lastDismissedPkg && (now - lastDismissedAtMs) < DEBOUNCE_MS) return

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
        super.onDestroy()
    }
}
