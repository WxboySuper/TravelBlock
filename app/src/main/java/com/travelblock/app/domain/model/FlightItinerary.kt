package com.travelblock.app.domain.model

data class FlightItinerary(
    val preset: TripPreset,
    val airports: List<Airport>,
    val focusMinutes: Int,
    val currentLeg: Int = 0,
) {
    init {
        require(airports.size == preset.legCount + 1)
        require(focusMinutes > 0)
        require(currentLeg in 0 until preset.legCount)
    }

    val isLastLeg: Boolean get() = currentLeg == preset.legCount - 1
    val layoverMinutes: Int get() = layoverMinutesAfter(currentLeg + 1)

    fun nextLeg(): FlightItinerary = copy(currentLeg = currentLeg + 1)

    companion object {
        const val STANDARD_LAYOVER_MINUTES = 5
        const val LONG_LAYOVER_MINUTES = 15
        const val LONG_LAYOVER_EVERY_LEGS = 4

        fun layoverMinutesAfter(completedLeg: Int): Int =
            if (completedLeg % LONG_LAYOVER_EVERY_LEGS == 0) LONG_LAYOVER_MINUTES
            else STANDARD_LAYOVER_MINUTES
    }
}
