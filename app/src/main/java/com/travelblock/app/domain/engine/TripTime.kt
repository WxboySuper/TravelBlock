package com.travelblock.app.domain.engine

import java.time.Duration
import java.time.LocalTime

/** Turns "land by 5:30" into the focus time a trip has to fit, breaks and boarding included. */
object TripTime {
    /** The booking flow schedules departure this long after check-in starts. */
    const val BOARDING_BUFFER_MINUTES = 8
    const val MIN_FOCUS_MINUTES = 15
    const val MAX_FOCUS_MINUTES = 720
    private const val MINUTES_PER_DAY = 24 * 60

    /** Minutes from [now] to the next time the clock reads [end], wrapping past midnight. */
    fun minutesUntil(end: LocalTime, now: LocalTime): Int {
        val minutes = Duration.between(now, end).toMinutes().toInt()
        return if (minutes <= 0) minutes + MINUTES_PER_DAY else minutes
    }

    /**
     * The most focus that fits in [elapsedMinutes] once boarding and the breaks a long session needs are taken
     * out. Short sessions have no breaks, so they fit exactly. Zero when there is no room for a trip.
     */
    fun focusForElapsed(elapsedMinutes: Int): Int {
        val room = elapsedMinutes - BOARDING_BUFFER_MINUTES
        var focus = room.coerceAtMost(MAX_FOCUS_MINUTES)
        while (focus >= MIN_FOCUS_MINUTES) {
            val breaks = AutoSessionPlan.summary(focus)?.breakMinutes ?: 0
            if (focus + breaks <= room) return focus
            focus--
        }
        return 0
    }
}
