package com.travelblock.app.domain.model

data class DraftFlightBooking(
    val origin: Airport,
    val destination: Airport,
    val durationMinutes: Int,
    val distanceMiles: Double,
    val estimatedRewardPoints: Int,
    val maxFlightTimeMinutes: Int = durationMinutes,
)
