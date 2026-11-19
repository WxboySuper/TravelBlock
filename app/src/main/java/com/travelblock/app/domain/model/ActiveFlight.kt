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
    val status: ActiveFlightStatus = ActiveFlightStatus.Active,
)
