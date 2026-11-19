package com.travelblock.app.domain.model

import java.time.Duration
import java.time.Instant
import kotlin.math.ceil

enum class FlightLivePhase { TAKEOFF, CRUISE, DESCENT, LAYOVER, ARRIVED }

data class FlightLiveSegments(
    val takeoff: Int = 5,
    val cruise: Int = 90,
    val descent: Int = 5,
) {
    val total: Int get() = takeoff + cruise + descent
}

data class FlightLiveUpdate(
    val title: String,
    val progressFraction: Float,
    val phase: FlightLivePhase,
    val statusText: String,
    val minutesRemaining: Int,
    val shortChipText: String,
    val segments: FlightLiveSegments,
) {
    val progressValue: Int get() = (progressFraction * segments.total).toInt()
}

object FlightLiveUpdateModel {
    private const val DESCENT_THRESHOLD_SECONDS = 120L
    private const val TAKEOFF_END = 0.05
    private const val DESCENT_START = 0.95

    fun create(flight: ActiveFlight, now: Instant): FlightLiveUpdate {
        if (flight.status == ActiveFlightStatus.Layover) return layoverUpdate(flight, now)
        val totalMillis = Duration.between(flight.startedAt, flight.plannedArrivalAt).toMillis().coerceAtLeast(1L)
        val elapsedMillis = Duration.between(flight.startedAt, now).toMillis()
        val remainingMillis = Duration.between(now, flight.plannedArrivalAt).toMillis().coerceAtLeast(0L)
        val progress = (elapsedMillis.toDouble() / totalMillis).coerceIn(0.0, 1.0)
        val phase = phaseFor(progress, remainingMillis)
        val minutes = ceil(remainingMillis / 60_000.0).toInt()
        return FlightLiveUpdate(
            title = "${flight.origin.displayCode} -> ${flight.destination.displayCode} / ${flight.flightNumber}",
            progressFraction = progress.toFloat(),
            phase = phase,
            statusText = if (phase == FlightLivePhase.DESCENT) "Descending" else "In flight",
            minutesRemaining = minutes,
            shortChipText = "${minutes}m",
            segments = FlightLiveSegments(),
        )
    }

    private fun layoverUpdate(flight: ActiveFlight, now: Instant): FlightLiveUpdate {
        val endAt = Instant.ofEpochMilli(flight.nextTransitionAtEpochMillis)
        val seconds = Duration.between(now, endAt).seconds.coerceAtLeast(0)
        val clock = "%02d:%02d".format(seconds / 60, seconds % 60)
        val minutes = ceil(seconds / 60.0).toInt()
        return FlightLiveUpdate(
            title = "Layover in ${flight.destination.displayCode}",
            progressFraction = 0f,
            phase = FlightLivePhase.LAYOVER,
            statusText = "Next leg in $clock",
            minutesRemaining = minutes,
            shortChipText = clock,
            segments = FlightLiveSegments(),
        )
    }

    private fun phaseFor(progress: Double, remainingMillis: Long): FlightLivePhase = when {
        progress >= 1.0 -> FlightLivePhase.ARRIVED
        remainingMillis <= DESCENT_THRESHOLD_SECONDS * 1_000 -> FlightLivePhase.DESCENT
        progress < TAKEOFF_END -> FlightLivePhase.TAKEOFF
        progress >= DESCENT_START -> FlightLivePhase.DESCENT
        else -> FlightLivePhase.CRUISE
    }
}
