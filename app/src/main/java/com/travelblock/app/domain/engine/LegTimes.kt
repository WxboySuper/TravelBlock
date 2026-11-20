package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.Airport

/**
 * How long each flight of a trip takes. A flight's time always follows from its distance, never from how much focus
 * time is wanted: a ten mile hop is a short flight and a long leg is a long one.
 */
object LegTimes {
    private val engine = AirportRouteEngine()

    /** Flight minutes for every leg between consecutive airports of [stops]. */
    fun forStops(stops: List<Airport>): List<Int> =
        stops.zipWithNext().map { (from, to) -> engine.estimatedDurationMinutes(engine.distanceMiles(from, to)) }
}
