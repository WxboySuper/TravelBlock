package com.travelblock.app.domain.model

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class ArrivalSummaryTest {
    @Test
    fun completedItinerarySummaryShowsTripTotalsAndFinalRoute() {
        val origin = airport("KOUN", "Norman")
        val layover = airport("KOKC", "Oklahoma City")
        val destination = airport("KTUL", "Tulsa")
        val flight = ActiveFlight(
            origin = layover,
            destination = destination,
            durationMinutes = 25,
            distanceMiles = 60.0,
            seat = SeatOption("10A", "10A", TicketClass.Standard, 0, "Window"),
            flightNumber = "TB123L2",
            startedAt = Instant.EPOCH,
            plannedArrivalAt = Instant.EPOCH.plusSeconds(25 * 60L),
            itineraryCodes = "KOUN,KOKC,KTUL",
            itineraryLegCount = 2,
            itineraryLegIndex = 1,
            itineraryDistanceMiles = 80.0,
            itineraryPointsEarned = 151,
        )

        val summary = ArrivalSummary.fromCompletedFlight(flight, pointsEarned = 200, routeAirports = listOf(origin, layover, destination))

        assertEquals(origin, summary.origin)
        assertEquals(destination, summary.destination)
        assertEquals(50, summary.durationMinutes)
        assertEquals(80.0, summary.distanceMiles, 0.0)
        assertEquals(351, summary.pointsEarned)
    }

    private fun airport(code: String, city: String) = Airport(
        code = code,
        icao = code,
        iata = code.takeLast(3),
        name = "$city Airport",
        city = city,
        stateOrRegion = null,
        country = "US",
        latitude = 35.0,
        longitude = -97.0,
        elevationFeet = null,
    )
}
