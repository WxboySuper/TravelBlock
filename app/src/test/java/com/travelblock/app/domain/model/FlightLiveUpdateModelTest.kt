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

        assertEquals("Layover in Norman", model.title)
        assertEquals("Next leg in 02:05", model.statusText)
        assertEquals("02:05", model.shortChipText)
        assertEquals(FlightLivePhase.LAYOVER, model.phase)
    }

    @Test
    fun aSingleFlightShowsTheInstrumentsAndNoTripBar() {
        val model = modelAt(600)

        assertTrue(model.detailText.contains(" ft / "))
        assertTrue(model.detailText.endsWith(" mph"))
        assertEquals(null, model.trip)
    }

    @Test
    fun aLongHaulSpansEveryFlightAndBreakInOneBar() {
        val trip = flight.copy(
            itineraryCodes = "KOUN,KOKC,KTUL",
            itineraryLegCount = 2,
            itineraryLegIndex = 1,
            itinerarySchedule = ActiveItinerarySchedule(listOf(30, 30), listOf(10)),
        )

        val bar = FlightLiveUpdateModel.create(trip, start.plusSeconds(600)).trip

        assertEquals(listOf(30, 10, 30), bar?.segments)
        assertEquals(70, bar?.totalMinutes)
        // The first flight and the break are done (40 minutes) and the second flight is 10 minutes in.
        assertEquals(50, bar?.progressMinutes)
    }

    @Test
    fun aLayoverCountsTheBreakAlreadyTaken() {
        val layover = flight.copy(
            status = ActiveFlightStatus.Layover,
            itineraryCodes = "KOUN,KOKC,KTUL",
            itineraryLegIndex = 0,
            itineraryLegCount = 2,
            itinerarySchedule = ActiveItinerarySchedule(listOf(30, 30), listOf(10)),
            layoverEndsAtEpochMillis = start.plusSeconds(6 * 60).toEpochMilli(),
        )

        val bar = FlightLiveUpdateModel.create(layover, start).trip

        // 30 minutes flown plus 4 of the 10 minute break (6 remain).
        assertEquals(34, bar?.progressMinutes)
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
