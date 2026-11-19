package com.travelblock.app.domain.model

data class ActiveItinerarySchedule(
    val legDurationsMinutes: List<Int>,
    val breakDurationsMinutes: List<Int> = emptyList(),
    val longBreakMinutes: Int? = null,
    val longBreakEvery: Int? = null,
) {
    fun breakAfter(legIndex: Int): Int = breakDurationsMinutes.getOrNull(legIndex)
        ?: configuredLongBreak(legIndex)
        ?: FlightItinerary.layoverMinutesAfter(legIndex + 1)

    private fun configuredLongBreak(legIndex: Int): Int? {
        val every = longBreakEvery ?: return null
        val minutes = longBreakMinutes ?: return null
        return minutes.takeIf { (legIndex + 1) % every == 0 }
    }
}
