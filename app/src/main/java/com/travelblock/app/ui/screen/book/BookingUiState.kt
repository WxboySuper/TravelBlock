package com.travelblock.app.ui.screen.book

import com.travelblock.app.domain.engine.HubConnection
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.BoardingPass
import com.travelblock.app.domain.model.SeatOption
import com.travelblock.app.domain.model.TripPreset
import com.travelblock.app.domain.model.SessionPlan
import com.travelblock.app.data.todoist.TodoistTask
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
    val todoistTaskId: String? = null,
    val todoistTaskContent: String? = null,
    val todoistProjectName: String? = null,
    val tripPreset: TripPreset? = null,
    val sessionPlan: SessionPlan? = null,
    val hub: HubConnection? = null,
    val hubChosen: Boolean = false,
    val stops: List<Airport> = emptyList(),
    val tripLocked: Boolean = false,
    /** True when each flight of the trip is flown for its own tasks. */
    val taskPerLeg: Boolean = false,
    val cabinSecured: Boolean = false,
    val materialsReady: Boolean = false,
    val distractionsStowed: Boolean = false,
    val bookingStage: BookingStage = BookingStage.Manifest,
) {
    val viaHub: Boolean get() = hub != null

    /** Every airport on the trip as far as it is known: the planned stops, or origin, hub and destination. */
    val tripStops: List<Airport>
        get() = stops.ifEmpty { listOfNotNull(origin, hub?.hub, destination).takeIf { hub != null }.orEmpty() }

    /** Where the first flight of the trip lands; the pre-flight screens are about this flight. */
    val legDestination: Airport? get() = tripStops.getOrNull(1) ?: destination

    val showTripCard: Boolean get() = !showSeatSelection && !tripLocked

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
                todoistTaskId = todoistTaskId,
                todoistTaskContent = todoistTaskContent,
                todoistProjectName = todoistProjectName,
                tripPreset = tripPreset,
                sessionPlan = sessionPlan,
                viaHubCode = hub?.hub?.code,
                stops = tripStops,
                taskPerLeg = taskPerLeg,
            )
        }

    val showTripOverview: Boolean
        get() = bookingStage == BookingStage.TripOverview

    val showSeatSelection: Boolean
        get() = bookingStage == BookingStage.SeatSelection

    val showBoardingPass: Boolean
        get() = bookingStage == BookingStage.BoardingPass

    val showGateCall: Boolean
        get() = bookingStage == BookingStage.GateCall
}
