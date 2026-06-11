package com.myniyam.app.service

import java.util.concurrent.ConcurrentHashMap

/**
 * Per-package grace window earned by completing a read (tapping Continue on the overlay).
 * While active, the accessibility service does not re-block that package.
 *
 * Pure logic: callers pass the clock (SystemClock.elapsedRealtime() in production) so the
 * window survives wall-clock changes and the class is unit-testable without mocks.
 */
object UnlockGrace {

    const val GRACE_MS = 5 * 60_000L

    private val grantedAt = ConcurrentHashMap<String, Long>()

    fun grant(pkg: String, nowMs: Long) {
        grantedAt[pkg] = nowMs
    }

    fun isActive(pkg: String, nowMs: Long): Boolean {
        val at = grantedAt[pkg] ?: return false
        return nowMs - at < GRACE_MS
    }

    fun clear() = grantedAt.clear()
}
