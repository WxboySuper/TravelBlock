package com.travelblock.app.ui.screen.book

import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.BoardingPass
import com.travelblock.app.domain.model.SeatOption
import java.time.LocalDateTime

data class BookingUiState(
    val draftAvailable: Boolean = false,
    val origin: Airport? = null,
    val destination: Airport? = null,
    val durationMinutes: Int = 0,
    val distanceMiles: Double = 0.0,
    val pointsRewardEstimate: Int = 0,
    val flightNumber: String = "",
    val gate: String = "",
    val boardingGroup: String = "",
    val departureTime: LocalDateTime? = null,
    val arrivalTime: LocalDateTime? = null,
    val seatOptions: List<SeatOption> = emptyList(),
    val selectedSeatId: String? = null,
    val pointsBalance: Int = 0,
    val seatSelectionMessage: String? = null,
    val maxFlightTimeMinutes: Int = 0,
    val focusObjective: String = "",
    val focusTag: String = "",
    val cabinSecured: Boolean = false,
    val materialsReady: Boolean = false,
    val distractionsStowed: Boolean = false,
    val bookingStage: BookingStage = BookingStage.Manifest,
) {
    val selectedSeat: SeatOption?
        get() = seatOptions.firstOrNull { it.id == selectedSeatId }

    val boardingPass: BoardingPass?
        get() {
            val origin = origin ?: return null
            val destination = destination ?: return null
            val seat = selectedSeat ?: return null
            val departure = departureTime ?: return null
            val arrival = arrivalTime ?: return null

            return BoardingPass(
                flightNumber = flightNumber,
                origin = origin,
                destination = destination,
                seat = seat,
                gate = gate,
                boardingGroup = boardingGroup,
                departureTime = departure,
                arrivalTime = arrival,
                focusDurationMinutes = durationMinutes,
                maxFlightTimeMinutes = maxFlightTimeMinutes,
                distanceMiles = distanceMiles,
                estimatedRewardPoints = pointsRewardEstimate,
                focusObjective = focusObjective,
                focusTag = focusTag,
            )
        }

    val showSeatSelection: Boolean
        get() = bookingStage == BookingStage.SeatSelection

    val showBoardingPass: Boolean
        get() = bookingStage == BookingStage.BoardingPass

    val showGateCall: Boolean
        get() = bookingStage == BookingStage.GateCall
}
