package com.travelblock.app.domain.model

data class FlightItinerary(
    val preset: TripPreset,
    val airports: List<Airport>,
    val schedule: ItinerarySchedule,
    val currentLeg: Int = 0,
) {
    init {
        require(airports.size == preset.legCount + 1)
        require(currentLeg in 0 until preset.legCount)
        require(schedule.focusMinutes > 0)
        require(schedule.legDurationsMinutes.size == preset.legCount && schedule.legDurationsMinutes.all { it > 0 })
        require(schedule.breakDurationsMinutes.isEmpty() || schedule.breakDurationsMinutes.size == preset.legCount - 1)
    }

    val focusMinutes: Int get() = schedule.focusMinutes
    val legDurationsMinutes: List<Int> get() = schedule.legDurationsMinutes
    val breakDurationsMinutes: List<Int> get() = schedule.breakDurationsMinutes
    val isLastLeg: Boolean get() = currentLeg == preset.legCount - 1
    val layoverMinutes: Int get() = breakDurationsMinutes.getOrNull(currentLeg) ?: layoverMinutesAfter(currentLeg + 1)
    val totalFocusMinutes: Int get() = legDurationsMinutes.sum()

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

data class ItinerarySchedule(
    val focusMinutes: Int,
    val legDurationsMinutes: List<Int>,
    val breakDurationsMinutes: List<Int> = emptyList(),
)
