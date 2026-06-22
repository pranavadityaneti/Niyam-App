package com.myniyam.app.service

import org.junit.Assert.assertEquals
import org.junit.Test

class PauseConfigTest {

    @Test fun `allowed minutes pass through`() {
        assertEquals(30, PauseConfig.sanitizeMinutes(30))
        assertEquals(60, PauseConfig.sanitizeMinutes(60))
        assertEquals(120, PauseConfig.sanitizeMinutes(120))
    }

    @Test fun `disallowed minutes fall back to 60`() {
        assertEquals(60, PauseConfig.sanitizeMinutes(45))
        assertEquals(60, PauseConfig.sanitizeMinutes(0))
        assertEquals(60, PauseConfig.sanitizeMinutes(-5))
        assertEquals(60, PauseConfig.sanitizeMinutes(1000))
    }

    @Test fun `pause seconds clamp to 15-60`() {
        assertEquals(15, PauseConfig.clampPauseSeconds(5))
        assertEquals(15, PauseConfig.clampPauseSeconds(15))
        assertEquals(20, PauseConfig.clampPauseSeconds(20))
        assertEquals(60, PauseConfig.clampPauseSeconds(60))
        assertEquals(60, PauseConfig.clampPauseSeconds(120))
    }

    @Test fun `interval millis uses sanitized minutes`() {
        assertEquals(30 * 60_000L, PauseConfig.intervalMillis(30))
        assertEquals(60 * 60_000L, PauseConfig.intervalMillis(60))
        assertEquals(120 * 60_000L, PauseConfig.intervalMillis(120))
        assertEquals(60 * 60_000L, PauseConfig.intervalMillis(45)) // fallback
    }
}
