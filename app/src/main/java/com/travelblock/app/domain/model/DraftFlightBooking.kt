package com.travelblock.app.domain.model

data class DraftFlightBooking(
    val origin: Airport,
    val destination: Airport,
    val durationMinutes: Int,
    val distanceMiles: Double,
    val estimatedRewardPoints: Int,
    val maxFlightTimeMinutes: Int = durationMinutes,
    val sessionPlan: SessionPlan? = null,
    /** A hub the planner already connected this trip through, and that the traveller has made their choice on. */
    val hub: com.travelblock.app.domain.engine.HubConnection? = null,
    val hubChoiceMade: Boolean = false,
    /** Set when this is the next leg of a long haul already under way, which skips check-in and starts at the seat. */
    val continuation: ActiveFlight? = null,
    /** Every airport on a planned trip, hubs included, when the traveller already chose the whole route. */
    val stops: List<Airport> = emptyList(),
    /** True when the trip was planned before check-in, so check-in only boards its first flight. */
    val tripLocked: Boolean = false,
) {
    /** Where the first flight lands: the first stop of a planned trip, else the destination. */
    val legDestination: Airport get() = stops.getOrNull(1) ?: hub?.hub ?: destination
}
