package com.travelblock.app.domain.model

import com.travelblock.app.domain.engine.FlightTelemetry
import com.travelblock.app.domain.engine.FlightTelemetryModel
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
    /** A second line of detail: the instruments in flight, or what to do at a layover. */
    val detailText: String = "",
    /** For a long haul, the whole trip as one bar: every flight and break, so progress spans the trip. */
    val trip: TripBar? = null,
) {
    val progressValue: Int get() = (progressFraction * segments.total).toInt()
}

/** One bar for a whole long haul. Flights and breaks alternate; [progressMinutes] is how far along the trip is. */
data class TripBar(val flightMinutes: List<Int>, val breakMinutes: List<Int>, val progressMinutes: Int) {
    /** Segment lengths in order: flight, break, flight, break, ..., flight. */
    val segments: List<Int>
        get() = flightMinutes.flatMapIndexed { index, minutes -> listOfNotNull(minutes, breakMinutes.getOrNull(index)) }

    val totalMinutes: Int get() = segments.sum()
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
        val telemetry = FlightTelemetryModel.at(flight, now)
        return FlightLiveUpdate(
            title = "${flight.origin.displayCode} to ${flight.destination.displayCode}",
            progressFraction = progress.toFloat(),
            phase = phase,
            statusText = if (phase == FlightLivePhase.DESCENT) "Descending" else "In flight",
            minutesRemaining = minutes,
            shortChipText = "${minutes}m",
            segments = FlightLiveSegments(),
            detailText = instrumentLine(telemetry),
            trip = tripBar(flight, elapsedMillis, now),
        )
    }

    /** "Cruising / 36,000 ft / 360 mph": the same numbers as the seat-back display, short enough for one line. */
    private fun instrumentLine(telemetry: FlightTelemetry): String =
        "${telemetry.stage.label} / ${"%,d".format(java.util.Locale.US, telemetry.altitudeFeet)} ft / ${telemetry.groundSpeedMph} mph"

    /** The trip as one bar with progress measured across every flight and break, or null for a single flight. */
    private fun tripBar(flight: ActiveFlight, elapsedLegMillis: Long, now: Instant): TripBar? {
        if (!flight.isItinerary) return null
        val flights = List(flight.itineraryLegCount) { flight.durationForLeg(it) }
        val breaks = List(flight.itineraryLegCount - 1) { flight.breakAfterLeg(it) }
        val done = (0 until flight.itineraryLegIndex).sumOf { flights[it] + breaks[it] }
        val into = if (flight.status == ActiveFlightStatus.Layover) {
            flights[flight.itineraryLegIndex] + layoverElapsedMinutes(flight, breaks[flight.itineraryLegIndex], now)
        } else {
            (elapsedLegMillis / 60_000L).toInt().coerceIn(0, flights[flight.itineraryLegIndex])
        }
        return TripBar(flights, breaks, done + into)
    }

    private fun layoverElapsedMinutes(flight: ActiveFlight, breakMinutes: Int, now: Instant): Int {
        val remaining = Duration.between(now, Instant.ofEpochMilli(flight.nextTransitionAtEpochMillis)).toMinutes().toInt()
        return (breakMinutes - remaining).coerceIn(0, breakMinutes)
    }

    private fun layoverUpdate(flight: ActiveFlight, now: Instant): FlightLiveUpdate {
        val endAt = Instant.ofEpochMilli(flight.nextTransitionAtEpochMillis)
        val seconds = Duration.between(now, endAt).seconds.coerceAtLeast(0)
        val clock = "%02d:%02d".format(seconds / 60, seconds % 60)
        val minutes = ceil(seconds / 60.0).toInt()
        return FlightLiveUpdate(
            title = "Layover in ${flight.destination.city.ifBlank { flight.destination.displayCode }}",
            progressFraction = 0f,
            phase = FlightLivePhase.LAYOVER,
            statusText = "Next leg in $clock",
            minutesRemaining = minutes,
            shortChipText = clock,
            segments = FlightLiveSegments(),
            detailText = "Pick your seat and board the next flight",
            trip = tripBar(flight, 0L, now),
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
