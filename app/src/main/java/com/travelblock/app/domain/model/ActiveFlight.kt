package com.travelblock.app.domain.model

import java.time.Instant

data class ActiveFlight(
    val origin: Airport,
    val destination: Airport,
    val durationMinutes: Int,
    val distanceMiles: Double,
    val seat: SeatOption,
    val flightNumber: String,
    val startedAt: Instant,
    val plannedArrivalAt: Instant,
    val focusObjective: String = "",
    val focusTag: String = "",
    val todoistTaskId: String? = null,
    val todoistTaskContent: String? = null,
    val todoistProjectName: String? = null,
    val itineraryCodes: String? = null,
    val itineraryLabel: String? = null,
    val itineraryLegIndex: Int = 0,
    val itineraryLegCount: Int = 1,
    val layoverEndsAtEpochMillis: Long? = null,
    val itineraryPointsEarned: Int = 0,
    val itineraryDistanceMiles: Double = 0.0,
    val status: ActiveFlightStatus = ActiveFlightStatus.Active,
) {
    val isItinerary: Boolean get() = itineraryCodes != null && itineraryLegCount > 1
    val isLastItineraryLeg: Boolean get() = !isItinerary || itineraryLegIndex >= itineraryLegCount - 1
    val nextTransitionAtEpochMillis: Long
        get() = if (status == ActiveFlightStatus.Layover) layoverEndsAtEpochMillis ?: plannedArrivalAt.toEpochMilli()
        else plannedArrivalAt.toEpochMilli()
}
