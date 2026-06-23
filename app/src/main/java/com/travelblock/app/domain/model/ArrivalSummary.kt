package com.travelblock.app.domain.model

data class ArrivalSummary(
    val origin: Airport,
    val destination: Airport,
    val flightNumber: String,
    val durationMinutes: Int,
    val distanceMiles: Double,
    val seat: SeatOption,
    val pointsEarned: Int,
    val status: ActiveFlightStatus,
    val focusObjective: String = "",
    val focusTag: String = "",
) {
    companion object {
        fun fromCompletedFlight(
            flight: ActiveFlight,
            pointsEarned: Int,
        ): ArrivalSummary {
            return ArrivalSummary(
                origin = flight.origin,
                destination = flight.destination,
                flightNumber = flight.flightNumber,
                durationMinutes = flight.durationMinutes,
                distanceMiles = flight.distanceMiles,
                seat = flight.seat,
                pointsEarned = pointsEarned,
                status = ActiveFlightStatus.Arrived,
                focusObjective = flight.focusObjective,
                focusTag = flight.focusTag,
            )
        }
    }
}
