package com.travelblock.app.domain.model

import java.time.LocalDateTime

data class BoardingPass(
    val flightNumber: String,
    val origin: Airport,
    val destination: Airport,
    val seat: SeatOption,
    val gate: String,
    val boardingGroup: String,
    val departureTime: LocalDateTime,
    val arrivalTime: LocalDateTime,
    val focusDurationMinutes: Int,
    val maxFlightTimeMinutes: Int,
    val distanceMiles: Double,
    val estimatedRewardPoints: Int,
    val focusObjective: String,
    val focusTag: String,
) {
    fun toActiveFlight(
        startedAt: java.time.Instant,
    ): ActiveFlight {
        return ActiveFlight(
            origin = origin,
            destination = destination,
            durationMinutes = focusDurationMinutes,
            distanceMiles = distanceMiles,
            seat = seat,
            flightNumber = flightNumber,
            startedAt = startedAt,
            plannedArrivalAt = startedAt.plusSeconds(focusDurationMinutes.toLong() * 60L),
            focusObjective = focusObjective,
            focusTag = focusTag,
        )
    }
}
