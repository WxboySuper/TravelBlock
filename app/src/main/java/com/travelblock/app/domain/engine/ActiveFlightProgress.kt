package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.ActiveFlight
import java.time.Duration
import java.time.Instant
import kotlin.math.roundToInt

data class ActiveFlightSnapshot(
    val progress: Double,
    val remaining: Duration,
    val elapsed: Duration,
    val milesFlown: Double,
    val milesRemaining: Double,
    val isComplete: Boolean,
)

object ActiveFlightProgress {
    fun snapshot(
        activeFlight: ActiveFlight,
        now: Instant,
    ): ActiveFlightSnapshot {
        val totalMillis = Duration.between(activeFlight.startedAt, activeFlight.plannedArrivalAt)
            .toMillis()
            .coerceAtLeast(1L)
        val elapsedMillis = Duration.between(activeFlight.startedAt, now)
            .toMillis()
            .coerceIn(0L, totalMillis)
        val remainingMillis = Duration.between(now, activeFlight.plannedArrivalAt)
            .toMillis()
            .coerceAtLeast(0L)
        val progress = (elapsedMillis.toDouble() / totalMillis.toDouble()).coerceIn(0.0, 1.0)
        val milesFlown = activeFlight.distanceMiles * progress

        return ActiveFlightSnapshot(
            progress = progress,
            remaining = Duration.ofMillis(remainingMillis),
            elapsed = Duration.ofMillis(elapsedMillis),
            milesFlown = milesFlown,
            milesRemaining = (activeFlight.distanceMiles - milesFlown).coerceAtLeast(0.0),
            isComplete = now >= activeFlight.plannedArrivalAt,
        )
    }
}

fun Duration.toClockLabel(): String {
    val totalSeconds = seconds.coerceAtLeast(0L)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

fun Double.toMilesLabel(): String = "${roundToInt()} mi"

