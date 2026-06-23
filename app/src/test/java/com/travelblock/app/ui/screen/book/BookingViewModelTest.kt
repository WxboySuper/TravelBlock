package com.travelblock.app.ui.screen.book

import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.DraftFlightBooking
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BookingViewModelTest {
    private val fixedClock = Clock.fixed(
        Instant.parse("2026-05-19T20:00:00Z"),
        ZoneId.of("America/Chicago"),
    )

    @Test
    fun createsBookingFromSelectedRoute() {
        val viewModel = BookingViewModel(draftFlightBooking = draft, clock = fixedClock)

        assertTrue(viewModel.uiState.draftAvailable)
        assertEquals("KOUN", viewModel.uiState.origin?.code)
        assertEquals("KOKC", viewModel.uiState.destination?.code)
        assertEquals(25, viewModel.uiState.durationMinutes)
        assertEquals(13.4, viewModel.uiState.distanceMiles, 0.01)
        assertEquals(126, viewModel.uiState.pointsRewardEstimate)
    }

    @Test
    fun generatesNonEmptyFlightNumberGateAndGroup() {
        val viewModel = BookingViewModel(draftFlightBooking = draft, clock = fixedClock)

        assertTrue(viewModel.uiState.flightNumber.isNotBlank())
        assertTrue(viewModel.uiState.gate.isNotBlank())
        assertTrue(viewModel.uiState.boardingGroup.isNotBlank())
    }

    @Test
    fun selectingSeatUpdatesState() {
        val viewModel = BookingViewModel(draftFlightBooking = draft, clock = fixedClock)
        val targetSeat = requireNotNull(viewModel.uiState.seatOptions.firstOrNull {
            it.isAvailable && it.pointsCost == 0 && it.id != viewModel.uiState.selectedSeatId
        })

        viewModel.selectSeat(targetSeat.id)

        assertEquals(targetSeat.id, viewModel.uiState.selectedSeatId)
        assertEquals(targetSeat.label, viewModel.uiState.selectedSeat?.label)
    }

    @Test
    fun boardingPassContainsCorrectRouteAndDuration() {
        val viewModel = BookingViewModel(draftFlightBooking = draft, pointsBalance = 45, clock = fixedClock)
        val targetSeat = requireNotNull(viewModel.uiState.seatOptions.firstOrNull {
            it.isAvailable && it.pointsCost == 45
        })

        viewModel.continueToSeatSelection()
        viewModel.selectSeat(targetSeat.id)
        viewModel.continueToBoardingPass()

        val boardingPass = viewModel.uiState.boardingPass
        assertNotNull(boardingPass)
        assertEquals("KOUN", boardingPass?.origin?.code)
        assertEquals("KOKC", boardingPass?.destination?.code)
        assertEquals(25, boardingPass?.focusDurationMinutes)
        assertEquals(targetSeat.label, boardingPass?.seat?.label)
        assertTrue(viewModel.uiState.showBoardingPass)
    }

    @Test
    fun cannotSkipDirectlyFromManifestToBoardingPassOrGateCall() {
        val viewModel = BookingViewModel(draftFlightBooking = draft, clock = fixedClock)

        viewModel.continueToBoardingPass()
        viewModel.boardFlight()

        assertFalse(viewModel.uiState.showBoardingPass)
        assertFalse(viewModel.uiState.showGateCall)
        assertEquals(BookingStage.Manifest, viewModel.uiState.bookingStage)
    }

    @Test
    fun paidSeatRequiresEnoughPoints() {
        val viewModel = BookingViewModel(draftFlightBooking = draft, pointsBalance = 0, clock = fixedClock)
        val originalSeat = viewModel.uiState.selectedSeatId

        val paidSeat = requireNotNull(viewModel.uiState.seatOptions.firstOrNull { it.isAvailable && it.pointsCost == 45 })
        viewModel.selectSeat(paidSeat.id)

        assertEquals(originalSeat, viewModel.uiState.selectedSeatId)
        assertTrue(viewModel.uiState.seatSelectionMessage.orEmpty().contains("needs 45 pts"))
    }

    @Test
    fun routeEstimatedDurationCarriesIntoBoardingPass() {
        val thirtySevenMinuteDraft = draft.copy(durationMinutes = 37, maxFlightTimeMinutes = 45, estimatedRewardPoints = 180)
        val viewModel = BookingViewModel(draftFlightBooking = thirtySevenMinuteDraft, clock = fixedClock)

        val boardingPass = viewModel.uiState.boardingPass

        assertEquals(37, viewModel.uiState.durationMinutes)
        assertEquals(45, viewModel.uiState.maxFlightTimeMinutes)
        assertEquals(37, boardingPass?.focusDurationMinutes)
        assertEquals(45, boardingPass?.maxFlightTimeMinutes)
    }

    @Test
    fun seatsIncludeUnavailableSeatsAndAtLeastOneFreeStandardSeat() {
        val viewModel = BookingViewModel(draftFlightBooking = draft, clock = fixedClock)

        assertTrue(viewModel.uiState.seatOptions.any { !it.isAvailable })
        assertTrue(viewModel.uiState.seatOptions.any { it.isAvailable && it.pointsCost == 0 })
    }

    @Test
    fun unavailableSeatCannotBeSelected() {
        val viewModel = BookingViewModel(draftFlightBooking = draft, clock = fixedClock)
        val originalSeat = viewModel.uiState.selectedSeatId
        val unavailableSeat = requireNotNull(viewModel.uiState.seatOptions.firstOrNull { !it.isAvailable })

        viewModel.selectSeat(unavailableSeat.id)

        assertEquals(originalSeat, viewModel.uiState.selectedSeatId)
    }

    @Test
    fun seatAvailabilityIsStableForSameBookingSeed() {
        val first = BookingViewModel(draftFlightBooking = draft, clock = fixedClock).uiState.seatOptions
        val second = BookingViewModel(draftFlightBooking = draft, clock = fixedClock).uiState.seatOptions

        assertEquals(first.map { it.id to it.isAvailable }, second.map { it.id to it.isAvailable })
    }

    @Test
    fun seatAvailabilityKeepsRowsAndSidesUsable() {
        val seats = BookingViewModel(draftFlightBooking = draft, clock = fixedClock).uiState.seatOptions

        seats.groupBy { it.label.dropLast(1).toInt() }.forEach { (_, rowSeats) ->
            assertTrue(rowSeats.count { it.isAvailable } >= 2)
            assertTrue(rowSeats.any { it.label.last() in listOf('A', 'B', 'C') && it.isAvailable })
            assertTrue(rowSeats.any { it.label.last() in listOf('D', 'E', 'F') && it.isAvailable })
        }
    }

    @Test
    fun focusObjectiveAndTagCanBeEmptyOrPresent() {
        val viewModel = BookingViewModel(draftFlightBooking = draft, clock = fixedClock)

        assertEquals("", viewModel.uiState.boardingPass?.focusObjective)
        viewModel.updateFocusObjective("Test homework block.")
        viewModel.selectFocusTag("Homework")

        assertEquals("Test homework block.", viewModel.uiState.boardingPass?.focusObjective)
        assertEquals("Homework", viewModel.uiState.boardingPass?.focusTag)
    }

    private val draft = DraftFlightBooking(
        origin = Airport(
            code = "KOUN",
            icao = "KOUN",
            iata = "OUN",
            name = "University of Oklahoma Westheimer Airport",
            city = "Norman",
            stateOrRegion = "Oklahoma",
            country = "US",
            latitude = 35.2456,
            longitude = -97.4721,
            elevationFeet = 1182,
        ),
        destination = Airport(
            code = "KOKC",
            icao = "KOKC",
            iata = "OKC",
            name = "Will Rogers World Airport",
            city = "Oklahoma City",
            stateOrRegion = "Oklahoma",
            country = "US",
            latitude = 35.3931,
            longitude = -97.6007,
            elevationFeet = 1295,
        ),
        durationMinutes = 25,
        distanceMiles = 13.4,
        estimatedRewardPoints = 126,
        maxFlightTimeMinutes = 45,
    )
}
