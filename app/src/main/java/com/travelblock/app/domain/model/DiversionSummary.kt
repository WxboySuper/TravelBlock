package com.travelblock.app.domain.model

import com.travelblock.app.domain.engine.ActiveFlightSnapshot
import java.time.Duration

data class DiversionSummary(
    val origin: Airport,
    val intendedDestination: Airport,
    val diversionAirport: Airport,
    val flightNumber: String,
    val focusedDuration: Duration,
    val milesFlown: Double,
    val milesRemainingSkipped: Double,
    val distanceToDiversionAirportMiles: Double,
    val percentComplete: Int,
    val pointsEarned: Int,
    val focusObjective: String = "",
    val focusTag: String = "",
    val status: ActiveFlightStatus = ActiveFlightStatus.Diverted,
) {
    companion object {
        fun fromFlight(
            flight: ActiveFlight,
            snapshot: ActiveFlightSnapshot,
            diversionAirport: Airport,
            distanceToDiversionAirportMiles: Double,
            pointsEarned: Int,
        ): DiversionSummary {
            return DiversionSummary(
                origin = flight.origin,
                intendedDestination = flight.destination,
                diversionAirport = diversionAirport,
                flightNumber = flight.flightNumber,
                focusedDuration = snapshot.elapsed,
                milesFlown = snapshot.milesFlown,
                milesRemainingSkipped = snapshot.milesRemaining,
                distanceToDiversionAirportMiles = distanceToDiversionAirportMiles,
                percentComplete = (snapshot.progress.coerceIn(0.0, 1.0) * 100).toInt(),
                pointsEarned = pointsEarned,
                focusObjective = flight.focusObjective,
                focusTag = flight.focusTag,
            )
        }
    }
}
