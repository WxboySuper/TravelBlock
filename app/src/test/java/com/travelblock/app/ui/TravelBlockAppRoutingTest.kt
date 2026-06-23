package com.travelblock.app.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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
}
