package com.travelblock.app.ui

import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.ActiveFlightStatus
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.SeatOption
import com.travelblock.app.domain.model.TicketClass
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TravelBlockAppEconomyTest {
    @Test
    fun noSeatChargeTransactionForStandardSeat() {
        val transaction = createSeatUpgradeTransaction(
            activeFlight = activeFlight.copy(
                seat = activeFlight.seat.copy(pointsCost = 0),
            ),
            now = Instant.parse("2026-05-21T21:00:00Z"),
        )

        assertNull(transaction)
    }

    @Test
    fun createsNegativeTransactionForPaidSeat() {
        val transaction = createSeatUpgradeTransaction(
            activeFlight = activeFlight.copy(
                seat = activeFlight.seat.copy(label = "3C", pointsCost = 45),
            ),
            now = Instant.parse("2026-05-21T21:00:00Z"),
        )

        requireNotNull(transaction)
        assertEquals(-45, transaction.amount)
        assertEquals("Seat upgrade 3C", transaction.reason)
    }

    private val activeFlight = ActiveFlight(
        origin = airport("KOUN", "Norman"),
        destination = airport("KTUL", "Tulsa"),
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
            elevationFeet = 1000,
        )
    }
}
