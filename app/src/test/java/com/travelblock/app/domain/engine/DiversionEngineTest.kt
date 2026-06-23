package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.SeatOption
import com.travelblock.app.domain.model.TicketClass
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class DiversionEngineTest {
    @Test
    fun findsClosestAirportToCurrentRoutePosition() {
        val result = DiversionEngine.findClosestDiversionAirport(
            flight = activeFlight,
            airports = listOf(
                airport("KAAA", "Near Midpoint", latitude = 0.0, longitude = 4.8),
                airport("KBBB", "Far Field", latitude = 10.0, longitude = 10.0),
                activeFlight.destination,
            ),
            progress = 0.5,
        )

        assertEquals("KAAA", result?.airport?.code)
    }

    @Test
    fun intendedDestinationIsNotAValidDiversionAirport() {
        val result = DiversionEngine.findClosestDiversionAirport(
            flight = activeFlight,
            airports = listOf(
                activeFlight.destination,
                activeFlight.origin,
            ),
            progress = 0.95,
        )

        assertNotEquals(activeFlight.destination.code, result?.airport?.code)
        assertEquals(activeFlight.origin.code, result?.airport?.code)
    }

    private val start = Instant.parse("2026-05-19T20:00:00Z")
    private val activeFlight = ActiveFlight(
        origin = airport("KOUN", "Origin", latitude = 0.0, longitude = 0.0),
        destination = airport("KOKC", "Destination", latitude = 0.0, longitude = 10.0),
        durationMinutes = 30,
        distanceMiles = 60.0,
        seat = SeatOption("18A", "18A", TicketClass.Standard, 0, "Standard aisle"),
        flightNumber = "TB123",
        startedAt = start,
        plannedArrivalAt = start.plusSeconds(30 * 60),
    )

    private fun airport(
        code: String,
        city: String,
        latitude: Double,
        longitude: Double,
    ): Airport {
        return Airport(
            code = code,
            icao = code,
            iata = code.takeLast(3),
            name = "$city Airport",
            city = city,
            stateOrRegion = "Oklahoma",
            country = "US",
            latitude = latitude,
            longitude = longitude,
            elevationFeet = 1_000,
        )
    }
}
