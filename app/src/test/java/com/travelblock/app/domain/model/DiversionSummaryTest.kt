package com.travelblock.app.domain.model

import com.travelblock.app.domain.engine.ActiveFlightProgress
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class DiversionSummaryTest {
    @Test
    fun calculatesPartialProgressFromSnapshot() {
        val snapshot = ActiveFlightProgress.snapshot(activeFlight, start.plusSeconds(15 * 60))
        val summary = DiversionSummary.fromFlight(
            flight = activeFlight,
            snapshot = snapshot,
            diversionAirport = airport("KTUL", "Tulsa"),
            distanceToDiversionAirportMiles = 42.0,
            pointsEarned = 20,
        )

        assertEquals(50, summary.percentComplete)
        assertEquals(30.0, summary.milesFlown, 0.001)
        assertEquals(30.0, summary.milesRemainingSkipped, 0.001)
        assertEquals("KTUL", summary.diversionAirport.code)
        assertEquals(42.0, summary.distanceToDiversionAirportMiles, 0.001)
        assertEquals(15L, summary.focusedDuration.toMinutes())
    }

    private val start = Instant.parse("2026-05-19T20:00:00Z")
    private val activeFlight = ActiveFlight(
        origin = airport("KOUN", "Norman"),
        destination = airport("KOKC", "Oklahoma City"),
        durationMinutes = 30,
        distanceMiles = 60.0,
        seat = SeatOption("18A", "18A", TicketClass.Standard, 0, "Standard aisle"),
        flightNumber = "TB123",
        startedAt = start,
        plannedArrivalAt = start.plusSeconds(30 * 60),
    )

    private fun airport(code: String, city: String): Airport {
        return Airport(
            code = code,
            icao = code,
            iata = code.takeLast(3),
            name = "$city Airport",
            city = city,
            stateOrRegion = "Oklahoma",
            country = "US",
            latitude = 35.0,
            longitude = -97.0,
            elevationFeet = 1_000,
        )
    }
}
