package com.travelblock.app.domain.model

data class FlightMapModel(
    val route: List<GeoPoint>,
    val aircraftPosition: GeoPoint,
    val progress: Double,
) {
    init {
        require(route.size >= 2) { "Route must include at least origin and destination points." }
        require(progress in 0.0..1.0) { "Progress must be between 0 and 1." }
    }
}
