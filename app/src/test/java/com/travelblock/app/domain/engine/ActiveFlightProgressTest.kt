package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.SeatOption
import com.travelblock.app.domain.model.TicketClass
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ActiveFlightProgressTest {
    @Test
    fun atStartProgressIsNearZero() {
        val snapshot = ActiveFlightProgress.snapshot(activeFlight, start)

        assertEquals(0.0, snapshot.progress, 0.001)
        assertEquals(60.0, snapshot.milesRemaining, 0.001)
    }

    @Test
    fun halfwayTimeGivesAboutFiftyPercent() {
        val snapshot = ActiveFlightProgress.snapshot(activeFlight, start.plusSeconds(15 * 60))

        assertEquals(0.5, snapshot.progress, 0.001)
        assertEquals(30.0, snapshot.milesFlown, 0.001)
        assertEquals(30.0, snapshot.milesRemaining, 0.001)
    }

    @Test
    fun afterPlannedArrivalIsComplete() {
        val snapshot = ActiveFlightProgress.snapshot(activeFlight, start.plusSeconds(40 * 60))

        assertTrue(snapshot.isComplete)
        assertEquals(1.0, snapshot.progress, 0.001)
        assertEquals(60.0, snapshot.milesFlown, 0.001)
    }

    @Test
    fun remainingTimeNeverGoesBelowZero() {
        val snapshot = ActiveFlightProgress.snapshot(activeFlight, start.plusSeconds(40 * 60))

        assertEquals(0L, snapshot.remaining.seconds)
    }

    @Test
    fun progressIsClampedBeforeStart() {
        val snapshot = ActiveFlightProgress.snapshot(activeFlight, start.minusSeconds(5 * 60))

        assertEquals(0.0, snapshot.progress, 0.001)
        assertEquals(0.0, snapshot.milesFlown, 0.001)
        assertEquals(60.0, snapshot.milesRemaining, 0.001)
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
