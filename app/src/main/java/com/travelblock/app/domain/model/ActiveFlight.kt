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
    val status: ActiveFlightStatus = ActiveFlightStatus.Active,
)
