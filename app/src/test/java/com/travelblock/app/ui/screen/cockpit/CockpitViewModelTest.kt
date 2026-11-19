package com.travelblock.app.ui.screen.cockpit

import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.ActiveFlightStatus
import com.travelblock.app.domain.model.ActiveItinerarySchedule
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.SeatOption
import com.travelblock.app.domain.model.TicketClass
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CockpitViewModelTest {
    @Test
    fun startFlightCreatesActiveState() {
        val clock = MutableClock(start)
        val viewModel = CockpitViewModel(activeFlight, clock, ZoneId.of("UTC"))

        assertTrue(viewModel.uiState.hasActiveFlight)
        assertEquals(ActiveFlightStatus.Active, viewModel.uiState.status)
        assertEquals("30:00", viewModel.uiState.timeRemainingLabel)
    }

    @Test
    fun completeChangesStateToArrived() {
        val clock = MutableClock(start)
        val viewModel = CockpitViewModel(activeFlight, clock, ZoneId.of("UTC"))

        clock.current = start.plusSeconds(31 * 60)
        viewModel.tick()

        assertEquals(ActiveFlightStatus.Arrived, viewModel.uiState.status)
        assertEquals("00:00", viewModel.uiState.timeRemainingLabel)
    }

    @Test
    fun completedTripLegStartsItsConfiguredBreak() {
        val clock = MutableClock(start)
        val trip = activeFlight.copy(
            itineraryCodes = "KOUN,KOKC,KTUL,KDAL",
            itineraryLegCount = 3,
            itinerarySchedule = ActiveItinerarySchedule(listOf(30, 30, 30), listOf(7, 13)),
        )
        val viewModel = CockpitViewModel(trip, clock, ZoneId.of("UTC"))

        clock.current = trip.plannedArrivalAt.plusSeconds(1)
        viewModel.tick()

        assertEquals(ActiveFlightStatus.Layover, viewModel.uiState.status)
        assertEquals(clock.current.plusSeconds(7 * 60L).toEpochMilli(), viewModel.uiState.activeFlight?.layoverEndsAtEpochMillis)
    }

    @Test
    fun divertChangesState() {
        val clock = MutableClock(start)
        val viewModel = CockpitViewModel(activeFlight, clock, ZoneId.of("UTC"))

        viewModel.requestDivert()
        assertTrue(viewModel.uiState.showDivertDialog)

        viewModel.confirmDivert()

        assertEquals(ActiveFlightStatus.Diverted, viewModel.uiState.status)
        assertEquals(false, viewModel.uiState.showDivertDialog)
    }

    @Test
    fun divertPopupSurvivesTheNextTick() {
        val clock = MutableClock(start)
        val viewModel = CockpitViewModel(activeFlight, clock, ZoneId.of("UTC"))

        viewModel.requestDivert()
        clock.current = start.plusSeconds(1)
        viewModel.tick()

        assertTrue(viewModel.uiState.showDivertDialog)
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

    private class MutableClock(
        var current: Instant,
        private val zone: ZoneId = ZoneId.of("UTC"),
    ) : Clock() {
        override fun getZone(): ZoneId = zone
        override fun withZone(zone: ZoneId): Clock = MutableClock(current, zone)
        override fun instant(): Instant = current
    }

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

