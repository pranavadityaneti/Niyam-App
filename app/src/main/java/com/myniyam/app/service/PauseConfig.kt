package com.myniyam.app.service

/**
 * Pure pause-behaviour math (SP-P-PAUSE). No android imports → unit-testable.
 * - interval check-in frequency is one of {30, 60, 120} minutes (else falls to 60)
 * - pause length (mantra read duration) is clamped to 15..60 seconds
 */
object PauseConfig {

    val ALLOWED_MINUTES = listOf(30, 60, 120)

    const val MIN_PAUSE_SECONDS = 15
    const val MAX_PAUSE_SECONDS = 60
    const val DEFAULT_PAUSE_SECONDS = 20

    fun sanitizeMinutes(minutes: Int): Int =
        if (minutes in ALLOWED_MINUTES) minutes else 60

    fun clampPauseSeconds(seconds: Int): Int =
        seconds.coerceIn(MIN_PAUSE_SECONDS, MAX_PAUSE_SECONDS)

    fun intervalMillis(minutes: Int): Long =
        sanitizeMinutes(minutes) * 60_000L
}
