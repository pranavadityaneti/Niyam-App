package com.myniyam.app.service

import android.os.Handler
import android.os.Looper

/**
 * Opt-in interval check-in (SP-P-PAUSE / forlater #14). While a blocked app stays
 * in the foreground, fires a repeating check-in every N minutes — so a single
 * open that turns into a long scroll still earns a mindful pause. It is INDEPENDENT
 * of [UnlockGrace] (which only governs the open-time trigger): grace silences the
 * launch overlay, the interval is the deliberate "still here?" nudge.
 *
 * Driven entirely by the AccessibilityService's window-state events:
 *   - onBlockedForeground(pkg): a blocked app is foreground → arm/keep the timer
 *   - onLeftBlocked(): foreground left all blocked apps → stop counting
 * The Handler runs on the main looper (the service's thread). Disabled = no-op.
 */
object IntervalCheckIn {

    private val handler = Handler(Looper.getMainLooper())
    @Volatile private var armedPkg: String? = null
    private var pending: Runnable? = null

    fun onBlockedForeground(
        pkg: String,
        enabled: Boolean,
        intervalMs: Long,
        onFire: (String) -> Unit
    ) {
        if (!enabled) { cancel(); return }
        if (armedPkg == pkg && pending != null) return  // already counting for this pkg — don't reset
        cancel()
        armedPkg = pkg
        schedule(pkg, intervalMs, onFire)
    }

    fun onLeftBlocked() = cancel()

    private fun schedule(pkg: String, intervalMs: Long, onFire: (String) -> Unit) {
        val r = Runnable {
            if (armedPkg != pkg) return@Runnable   // left the app before firing
            onFire(pkg)
            schedule(pkg, intervalMs, onFire)       // re-arm for the next interval
        }
        pending = r
        handler.postDelayed(r, intervalMs)
    }

    private fun cancel() {
        pending?.let(handler::removeCallbacks)
        pending = null
        armedPkg = null
    }
}
