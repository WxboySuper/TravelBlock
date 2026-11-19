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
    val todoistTaskId: String? = null,
    val todoistTaskContent: String? = null,
) {
    companion object {
        fun fromCompletedFlight(
            flight: ActiveFlight,
            pointsEarned: Int,
            routeAirports: List<Airport> = emptyList(),
        ): ArrivalSummary {
            val routeCodes = flight.itineraryCodes?.split(',').orEmpty()
            return ArrivalSummary(
                origin = routeAirports.firstOrNull { it.code == routeCodes.firstOrNull() } ?: flight.origin,
                destination = routeAirports.firstOrNull { it.code == routeCodes.lastOrNull() } ?: flight.destination,
                flightNumber = flight.flightNumber,
                durationMinutes = flight.durationMinutes * flight.itineraryLegCount,
                distanceMiles = if (flight.isItinerary) flight.itineraryDistanceMiles else flight.distanceMiles,
                seat = flight.seat,
                pointsEarned = pointsEarned + flight.itineraryPointsEarned,
                status = ActiveFlightStatus.Arrived,
                focusObjective = flight.focusObjective,
                focusTag = flight.focusTag,
                todoistTaskId = flight.todoistTaskId,
                todoistTaskContent = flight.todoistTaskContent,
            )
        }
    }
}
