package com.travelblock.app.domain.model

data class TripPreset(val id: String, val label: String, val legCount: Int) {
    init {
        require(legCount in 2..MAX_TRIP_LEGS)
    }

    companion object {
        const val MAX_TRIP_LEGS = 12
        val presets = listOf(
            TripPreset("short", "Short haul", 2),
            TripPreset("domestic", "Domestic", 3),
            TripPreset("transcontinental", "Transcontinental", 4),
            TripPreset("transatlantic", "Transatlantic", 6),
            TripPreset("transpacific", "Transpacific", 8),
            TripPreset("round-the-world", "Round-the-world", 12),
        )
    }
}
