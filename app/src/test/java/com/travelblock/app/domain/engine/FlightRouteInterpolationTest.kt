package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.ActiveFlightStatus
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.GeoPoint
import com.travelblock.app.domain.model.SeatOption
import com.travelblock.app.domain.model.TicketClass
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FlightRouteInterpolationTest {
    @Test
    fun buildRouteIncludesEndpoints() {
        val origin = GeoPoint(latitude = 35.2208, longitude = -97.4395)
        val destination = GeoPoint(latitude = 36.1984, longitude = -95.8881)

        val route = FlightRouteInterpolation.buildRoute(origin, destination, sampleCount = 8)

        assertEquals(8, route.size)
        assertEquals(origin, route.first())
        assertEquals(destination, route.last())
    }

    @Test
    fun mapModelReturnsInterpolatedAircraftPosition() {
        val model = FlightRouteInterpolation.mapModelForFlight(
            activeFlight = activeFlight,
            progress = 0.5,
            sampleCount = 24,
        )

        assertEquals(24, model.route.size)
        assertEquals(0.5, model.progress, 0.0001)
        assertTrue(model.aircraftPosition.latitude in 35.0..37.0)
        assertTrue(model.aircraftPosition.longitude in -98.0..-95.0)
    }

    private val activeFlight = ActiveFlight(
        origin = airport(
            code = "KOUN",
            city = "Norman",
            latitude = 35.2208,
            longitude = -97.4395,
        ),
        destination = airport(
            code = "KTUL",
            city = "Tulsa",
            latitude = 36.1984,
            longitude = -95.8881,
        ),
        durationMinutes = 23,
        distanceMiles = 111.0,
        seat = SeatOption(
            id = "10C",
            label = "10C",
            ticketClass = TicketClass.Standard,
            pointsCost = 0,
            description = "Aisle",
        ),
        flightNumber = "TB443",
        startedAt = Instant.parse("2026-05-21T20:00:00Z"),
        plannedArrivalAt = Instant.parse("2026-05-21T20:23:00Z"),
        status = ActiveFlightStatus.Active,
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
            elevationFeet = 1000,
        )
    }
}
