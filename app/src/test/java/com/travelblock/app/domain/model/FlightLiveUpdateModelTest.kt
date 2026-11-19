package com.travelblock.app.domain.model

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FlightLiveUpdateModelTest {
    @Test
    fun clampsBeforeStartAndUsesTakeoffPhase() {
        val model = FlightLiveUpdateModel.create(flight, start.minusSeconds(30))

        assertEquals(0f, model.progressFraction)
        assertEquals(FlightLivePhase.TAKEOFF, model.phase)
    }

    @Test
    fun usesTakeoffAndCruiseBoundaries() {
        assertEquals(FlightLivePhase.TAKEOFF, modelAt(60).phase)
        assertEquals(FlightLivePhase.CRUISE, modelAt(180).phase)
        assertEquals(FlightLiveSegments(), modelAt(180).segments)
    }

    @Test
    fun switchesToDescendingAtTwoMinutesRemaining() {
        val model = FlightLiveUpdateModel.create(flight, flight.plannedArrivalAt.minusSeconds(120))

        assertEquals("Descending", model.statusText)
        assertEquals("2m", model.shortChipText)
    }

    @Test
    fun clampsAtArrival() {
        val model = FlightLiveUpdateModel.create(flight, flight.plannedArrivalAt)

        assertEquals(1f, model.progressFraction)
        assertEquals(FlightLivePhase.ARRIVED, model.phase)
        assertEquals(0, model.minutesRemaining)
    }

    @Test
    fun veryShortFlightsKeepValidProgressAndLandingPhase() {
        val shortFlight = flight.copy(plannedArrivalAt = start.plusSeconds(20))
        val beforeArrival = FlightLiveUpdateModel.create(shortFlight, start.plusSeconds(10))
        val atArrival = FlightLiveUpdateModel.create(shortFlight, start.plusSeconds(20))

        assertTrue(beforeArrival.progressFraction in 0f..1f)
        assertEquals(FlightLivePhase.DESCENT, beforeArrival.phase)
        assertEquals(FlightLivePhase.ARRIVED, atArrival.phase)
    }

    @Test
    fun layoverLiveUpdateNamesAirportAndNextLegCountdown() {
        val layover = flight.copy(
            status = ActiveFlightStatus.Layover,
            itineraryCodes = "KOUN,KOKC,KTUL",
            itineraryLegIndex = 0,
            itineraryLegCount = 2,
            layoverEndsAtEpochMillis = start.plusSeconds(125).toEpochMilli(),
        )

        val model = FlightLiveUpdateModel.create(layover, start)

        assertEquals("Layover in TUL", model.title)
        assertEquals("Next leg in 02:05", model.statusText)
        assertEquals("02:05", model.shortChipText)
        assertEquals(FlightLivePhase.LAYOVER, model.phase)
    }

    private fun modelAt(elapsedSeconds: Long) = FlightLiveUpdateModel.create(flight, start.plusSeconds(elapsedSeconds))

    private val start = Instant.parse("2026-05-19T20:00:00Z")
    private val airport = Airport("KOUN", "KOUN", "OUN", "Airport", "Norman", "Oklahoma", "US", 35.0, -97.0, 1_000)
    private val flight = ActiveFlight(
        origin = airport,
        destination = airport.copy(code = "KTUL", icao = "KTUL", iata = "TUL"),
        durationMinutes = 30,
        distanceMiles = 100.0,
        seat = SeatOption("10B", "10B", TicketClass.Standard, 0, "Middle seat"),
        flightNumber = "TB443",
        startedAt = start,
        plannedArrivalAt = start.plusSeconds(30 * 60),
    )
}
