package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.ActiveFlightStatus
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.BoardingPass
import com.travelblock.app.domain.model.DraftFlightBooking
import java.time.Instant

/**
 * The legs of a long haul after the first. Every leg is its own flight: you pick a seat, get a boarding pass and
 * board again, so the next leg is first offered as a booking draft and only becomes a flight once it is boarded.
 */
object ItineraryLegs {
    /** The booking for the leg after the one just flown, or null when the trip has no more legs. */
    fun nextLegDraft(flight: ActiveFlight, airports: List<Airport>): DraftFlightBooking? {
        val codes = flight.itineraryCodes?.split(',') ?: return null
        val index = flight.itineraryLegIndex + 1
        val origin = airports.firstOrNull { it.code == codes.getOrNull(index) } ?: return null
        val destination = airports.firstOrNull { it.code == codes.getOrNull(index + 1) } ?: return null
        val minutes = flight.durationForLeg(index)
        val distance = AirportRouteEngine().distanceMiles(origin, destination)
        return DraftFlightBooking(
            origin = origin,
            destination = destination,
            durationMinutes = minutes,
            distanceMiles = distance,
            estimatedRewardPoints = FlightRewardCalculator.completedReward(minutes, distance),
            maxFlightTimeMinutes = minutes,
            hubChoiceMade = true,
            continuation = flight,
        )
    }

    /** The flight for a boarded next leg: the new seat and flight number, the trip's notes and schedule carried on. */
    fun startNextLeg(flight: ActiveFlight, pass: BoardingPass, startedAt: Instant): ActiveFlight {
        val index = flight.itineraryLegIndex + 1
        val minutes = flight.durationForLeg(index)
        return flight.copy(
            origin = pass.origin,
            destination = pass.destination,
            distanceMiles = pass.distanceMiles,
            seat = pass.seat,
            flightNumber = pass.flightNumber,
            startedAt = startedAt,
            plannedArrivalAt = startedAt.plusSeconds(minutes * SECONDS_PER_MINUTE),
            itineraryLegIndex = index,
            durationMinutes = minutes,
            layoverEndsAtEpochMillis = null,
            status = ActiveFlightStatus.Active,
        )
    }

    private const val SECONDS_PER_MINUTE = 60L
}
