package com.travelblock.app.domain.engine

import com.travelblock.app.data.database.FlightLogEntity
import com.travelblock.app.data.database.FlightLogStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class PilotRank(val title: String, val hoursRequired: Int)
data class RankProgress(val rank: PilotRank, val nextRank: PilotRank?, val progress: Float, val hoursRemaining: Double)
data class WeekFlightStats(val hours: Double, val flights: Int, val distanceMiles: Double, val airportsVisited: Int)
data class PilotSummary(val rank: RankProgress, val currentStreak: Int, val bestStreak: Int, val weekly: WeekFlightStats, val airportsVisited: List<String>, val unlockedBadges: Set<String>)

object PilotProgressionEngine {
    val ranks = listOf(
        PilotRank("Student Pilot", 0), PilotRank("Private Pilot", 10), PilotRank("Commercial Pilot", 50),
        PilotRank("First Officer", 150), PilotRank("Captain", 400), PilotRank("Airline Legend", 1000),
    )
    val badges = listOf("First Flight", "Ten Flights", "Red-eye", "Transatlantic", "Airport Collector", "Seven-day Streak", "Clear Skies")

    fun summarize(flights: List<FlightLogEntity>, zone: ZoneId = ZoneId.systemDefault(), now: Instant = Instant.now()): PilotSummary {
        val completed = flights.filter { it.status == FlightLogStatus.COMPLETED.name }
        val hours = completed.sumOf { it.durationMinutes }.toDouble() / 60.0
        val rankIndex = ranks.indexOfLast { hours >= it.hoursRequired }.coerceAtLeast(0)
        val rank = ranks[rankIndex]
        val next = ranks.getOrNull(rankIndex + 1)
        val progress = if (next == null) 1f else ((hours - rank.hoursRequired) / (next.hoursRequired - rank.hoursRequired)).toFloat().coerceIn(0f, 1f)
        val days = completed.map { Instant.ofEpochMilli(it.endedAt).atZone(zone).toLocalDate() }.toSet()
        val today = now.atZone(zone).toLocalDate()
        val streakStart = if (today in days) today else today.minusDays(1)
        var streak = 0
        var date = streakStart
        while (date in days) { streak++; date = date.minusDays(1) }
        val weekStart = today.minusDays((today.dayOfWeek.value - 1).toLong())
        val weeklyFlights = completed.filter { Instant.ofEpochMilli(it.endedAt).atZone(zone).toLocalDate() in weekStart..today }
        val airports = completed.flatMap { listOf(it.originCode, it.destinationCode) }.filter(String::isNotBlank).distinct().sorted()
        val allFlights = flights.sortedBy { it.endedAt }
        val clearRun = allFlights.asReversed().takeWhile { it.status == FlightLogStatus.COMPLETED.name }.size
        val unlocked = buildSet {
            if (completed.isNotEmpty()) add("First Flight")
            if (completed.size >= 10) add("Ten Flights")
            if (completed.any { val h = Instant.ofEpochMilli(it.startedAt).atZone(zone).hour; h >= 22 || h < 5 }) add("Red-eye")
            if (completed.any { it.distanceMiles >= 3000.0 }) add("Transatlantic")
            if (airports.size >= 25) add("Airport Collector")
            if (streak >= 7 || bestStreak(days) >= 7) add("Seven-day Streak")
            if (clearRun >= 5) add("Clear Skies")
        }
        return PilotSummary(RankProgress(rank, next, progress, ((next?.hoursRequired ?: rank.hoursRequired).toDouble() - hours).coerceAtLeast(0.0)), streak, bestStreak(days), WeekFlightStats(weeklyFlights.sumOf { it.durationMinutes } / 60.0, weeklyFlights.size, weeklyFlights.sumOf { it.distanceMiles }, weeklyFlights.flatMap { listOf(it.originCode, it.destinationCode) }.distinct().size), airports, unlocked)
    }

    private fun bestStreak(days: Set<LocalDate>): Int {
        var best = 0; var run = 0; var previous: LocalDate? = null
        days.sorted().forEach { day -> run = if (previous?.plusDays(1) == day) run + 1 else 1; best = maxOf(best, run); previous = day }
        return best
    }
}
