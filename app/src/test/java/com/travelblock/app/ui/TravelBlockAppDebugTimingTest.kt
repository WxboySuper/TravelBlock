package com.travelblock.app.ui

import com.travelblock.app.data.settings.UserSettings
import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.SeatOption
import com.travelblock.app.domain.model.TicketClass
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class TravelBlockAppDebugTimingTest {
    @Test
    fun enabledDebugFlightUsesConfiguredSeconds() {
        val flight = activeFlight.withDebugFlightDuration(
            UserSettings(debugShortFlightsEnabled = true, debugFlightDurationSeconds = 20),
        )

        assertEquals(20, flight.plannedArrivalAt.epochSecond - flight.startedAt.epochSecond)
        assertEquals(25, flight.durationMinutes)
    }

    @Test
    fun disabledDebugFlightKeepsNormalDuration() {
        val flight = activeFlight.withDebugFlightDuration(UserSettings(debugShortFlightsEnabled = false))
        assertEquals(25 * 60L, flight.plannedArrivalAt.epochSecond - flight.startedAt.epochSecond)
    }

    private val airport = Airport(
        code = "KOUN", icao = "KOUN", iata = "OUN", name = "Westheimer Airport", city = "Norman",
        stateOrRegion = "Oklahoma", country = "US", latitude = 35.2, longitude = -97.4, elevationFeet = 1000,
    )
    private val activeFlight = ActiveFlight(
        origin = airport,
        destination = airport.copy(code = "KOKC", icao = "KOKC"),
        durationMinutes = 25,
        distanceMiles = 13.0,
        seat = SeatOption("10A", "10A", TicketClass.Standard, 0, "Window"),
        flightNumber = "TB101",
        startedAt = Instant.parse("2026-11-18T15:00:00Z"),
        plannedArrivalAt = Instant.parse("2026-11-18T15:25:00Z"),
    )
}
