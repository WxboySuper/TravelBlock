package com.travelblock.app.ui.screen.book

import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.DraftFlightBooking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class QuickBoardingScreenTest {
    @Test
    fun quickPassKeepsTheChosenRouteAndAssignsAFreeDefaultSeat() {
        val now = LocalDateTime.of(2026, 11, 18, 9, 0)
        val pass = createQuickBoardingPass(draft, now)

        assertEquals("KOUN", pass.origin.code)
        assertEquals("KOKC", pass.destination.code)
        assertEquals(15, pass.focusDurationMinutes)
        assertEquals("10A", pass.seat.label)
        assertEquals(0, pass.seat.pointsCost)
        assertTrue(pass.seat.isAvailable)
        assertEquals(now.plusMinutes(1), pass.departureTime)
        assertEquals(now.plusMinutes(16), pass.arrivalTime)
        assertEquals(45, pass.maxFlightTimeMinutes)
    }

    private val draft = DraftFlightBooking(
        origin = airport("KOUN", "Norman"),
        destination = airport("KOKC", "Oklahoma City"),
        durationMinutes = 15,
        distanceMiles = 13.0,
        estimatedRewardPoints = 30,
        maxFlightTimeMinutes = 45,
    )

    private fun airport(code: String, city: String) = Airport(
        code = code,
        icao = code,
        iata = null,
        name = "$city Airport",
        city = city,
        stateOrRegion = "Oklahoma",
        country = "US",
        latitude = 35.0,
        longitude = -97.0,
        elevationFeet = 1_000,
    )
}
