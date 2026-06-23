package com.travelblock.app.domain.engine

/**
 * Tunable constants for translating focus minutes into simulated route distance.
 */
object RouteEngineConfig {
    const val SimulatedCruiseSpeedMph = 360.0
    const val ShortRouteCruiseSpeedMph = 285.0
    const val TaxiOutMinutes = 5
    const val ClimbAndDescentMinutes = 7
    const val MinimumFlightTimeMinutes = 5
    const val MinimumDistanceMultiplier = 0.0
    const val MaximumDistanceMultiplier = 1.25
    const val BestMatchTolerance = 0.22
    const val LongStretchThreshold = 0.86
    const val LongStretchTimeThreshold = 0.86
    const val MajorAirportPriorityBoost = 0.68
    const val RegionalAirportPriorityBoost = 0.42
    const val NamedAirportPriorityBoost = 0.18
    const val CompleteCodePriorityBoost = 0.12

    val MajorAirportNameSignals = listOf(
        "international",
        "world",
        "continental",
        "continential",
        "intercontinental",
        "global",
    )

    val RegionalAirportNameSignals = listOf(
        "regional",
        "municipal",
        "metro",
        "metropolitan",
        "city",
    )

    val NamedAirportSignals = listOf(
        "airport",
        "field",
        "airfield",
    )
}
