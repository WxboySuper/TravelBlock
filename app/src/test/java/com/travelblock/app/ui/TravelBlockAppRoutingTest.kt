package com.travelblock.app.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class TravelBlockAppRoutingTest {
    @Test
    fun autoRouteEnabledWhenFlightRestoredAndOnHome() {
        val shouldRoute = shouldAutoRouteToCockpit(
            activeFlightRestored = true,
            hasActiveFlight = true,
            currentRoute = "home",
            restoreRouteHandled = false,
        )

        assertTrue(shouldRoute)
    }

    @Test
    fun autoRouteDisabledWhenAlreadyHandled() {
        val shouldRoute = shouldAutoRouteToCockpit(
            activeFlightRestored = true,
            hasActiveFlight = true,
            currentRoute = "home",
            restoreRouteHandled = true,
        )

        assertFalse(shouldRoute)
    }

    @Test
    fun autoRouteDisabledWithoutActiveFlight() {
        val shouldRoute = shouldAutoRouteToCockpit(
            activeFlightRestored = true,
            hasActiveFlight = false,
            currentRoute = "home",
            restoreRouteHandled = false,
        )

        assertFalse(shouldRoute)
    }

    @Test
    fun expiredRestoredFlightGoesToArrival() {
        assertTrue(shouldRestoreAsArrival(Instant.parse("2026-01-01T10:00:00Z"), Instant.parse("2026-01-01T10:00:01Z")))
    }

    @Test
    fun flightAtLandingTimeGoesToArrival() {
        val landingTime = Instant.parse("2026-01-01T10:00:00Z")
        assertTrue(shouldRestoreAsArrival(landingTime, landingTime))
    }

    @Test
    fun flightBeforeLandingRestoresToCockpit() {
        assertFalse(shouldRestoreAsArrival(Instant.parse("2026-01-01T10:00:00Z"), Instant.parse("2026-01-01T09:59:59Z")))
    }
}
