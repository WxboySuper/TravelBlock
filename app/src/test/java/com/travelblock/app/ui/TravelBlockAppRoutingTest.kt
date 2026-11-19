package com.travelblock.app.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.ActiveFlightStatus
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.SeatOption
import com.travelblock.app.domain.model.TicketClass

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

    @Test
    fun expiredLayoverRestoresToCockpitInsteadOfArrival() {
        val now = Instant.parse("2026-01-01T10:00:01Z")
        val airport = Airport("KAAA", "KAAA", "AAA", "Airport", "City", null, "US", 0.0, 0.0, null)
        val layover = ActiveFlight(
            origin = airport,
            destination = airport.copy(code = "KBBB"),
            durationMinutes = 25,
            distanceMiles = 100.0,
            seat = SeatOption("1A", "1A", TicketClass.Standard, 0, "Window"),
            flightNumber = "TB123L1",
            startedAt = now.minusSeconds(30 * 60),
            plannedArrivalAt = now.minusSeconds(5 * 60),
            itineraryCodes = "KAAA,KBBB,KCCC",
            itineraryLegCount = 2,
            itineraryLegIndex = 0,
            layoverEndsAtEpochMillis = now.minusSeconds(1).toEpochMilli(),
            status = ActiveFlightStatus.Layover,
        )

        assertFalse(shouldRestoreAsArrival(layover, now))
    }
}
