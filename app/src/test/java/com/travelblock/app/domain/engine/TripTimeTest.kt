package com.travelblock.app.domain.engine

import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TripTimeTest {
    @Test
    fun `minutes until an earlier clock time wraps to tomorrow`() {
        assertEquals(60, TripTime.minutesUntil(LocalTime.of(10, 0), LocalTime.of(9, 0)))
        assertEquals(23 * 60, TripTime.minutesUntil(LocalTime.of(9, 0), LocalTime.of(10, 0)))
    }

    @Test
    fun `a short window is all focus once boarding is out`() {
        assertEquals(60 - TripTime.BOARDING_BUFFER_MINUTES, TripTime.focusForElapsed(60))
    }

    @Test
    fun `a long window leaves room for the breaks`() {
        val elapsed = 300
        val focus = TripTime.focusForElapsed(elapsed)
        val breaks = AutoSessionPlan.summary(focus)?.breakMinutes ?: 0

        assertTrue(focus < elapsed - TripTime.BOARDING_BUFFER_MINUTES)
        assertTrue(focus + breaks <= elapsed - TripTime.BOARDING_BUFFER_MINUTES)
    }

    @Test
    fun `a window too small for a trip gives zero`() {
        assertEquals(0, TripTime.focusForElapsed(15))
    }
}
