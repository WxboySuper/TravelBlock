package com.travelblock.app.domain.model

data class FlightRoute(
    val origin: Airport,
    val destination: Airport,
    val distanceMiles: Double,
    val targetDistanceMiles: Double,
    val selectedDurationMinutes: Int,
    val estimatedDurationMinutes: Int,
    val availability: RouteAvailabilityResult,
)

