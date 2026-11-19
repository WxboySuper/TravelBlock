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
    val todoistTaskId: String? = null,
    val todoistTaskContent: String? = null,
    val todoistProjectName: String? = null,
    val tripPreset: TripPreset? = null,
    val sessionPlan: SessionPlan? = null,
    /** The hub this trip connects through, when the traveller chose to connect a short trip. */
    val viaHubCode: String? = null,
    /** Every airport on the trip when it has stops, so the pass can say where this first flight lands. */
    val stops: List<Airport> = emptyList(),
) {
    /** Where this flight lands: the first stop of a trip with stops, else the destination. */
    val legDestination: Airport get() = stops.getOrNull(1) ?: destination

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
            todoistTaskId = todoistTaskId,
            todoistTaskContent = todoistTaskContent,
            todoistProjectName = todoistProjectName,
        )
    }
}
