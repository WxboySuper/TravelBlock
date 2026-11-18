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
        val completed = flights.filter { it.isCompleted() }
        val hours = completed.sumOf { it.durationMinutes }.toDouble() / 60.0
        val days = completed.map { it.endedAt.toLocalDate(zone) }.toSet()
        val today = now.atZone(zone).toLocalDate()
        val streak = currentStreak(days, today)
        val best = bestStreak(days)
        val airports = airportsVisited(completed)
        val unlocked = unlockedBadges(flights, completed, maxOf(streak, best), zone)
        return PilotSummary(rankProgress(hours), streak, best, weeklyStats(completed, today, zone), airports, unlocked)
    }

    private fun FlightLogEntity.isCompleted() = status == FlightLogStatus.COMPLETED.name

    private fun Long.toLocalDate(zone: ZoneId): LocalDate = Instant.ofEpochMilli(this).atZone(zone).toLocalDate()

    private fun rankProgress(hours: Double): RankProgress {
        val rankIndex = ranks.indexOfLast { hours >= it.hoursRequired }.coerceAtLeast(0)
        val rank = ranks[rankIndex]
        val next = ranks.getOrNull(rankIndex + 1)
        val progress = if (next == null) 1f else ((hours - rank.hoursRequired) / (next.hoursRequired - rank.hoursRequired)).toFloat().coerceIn(0f, 1f)
        val remaining = ((next?.hoursRequired ?: rank.hoursRequired).toDouble() - hours).coerceAtLeast(0.0)
        return RankProgress(rank, next, progress, remaining)
    }

    private fun currentStreak(days: Set<LocalDate>, today: LocalDate): Int {
        var date = if (today in days) today else today.minusDays(1)
        var streak = 0
        while (date in days) { streak++; date = date.minusDays(1) }
        return streak
    }

    private fun weeklyStats(completed: List<FlightLogEntity>, today: LocalDate, zone: ZoneId): WeekFlightStats {
        val weekStart = today.minusDays((today.dayOfWeek.value - 1).toLong())
        val week = completed.filter { it.endedAt.toLocalDate(zone) in weekStart..today }
        val airports = week.flatMap { listOf(it.originCode, it.destinationCode) }.distinct().size
        return WeekFlightStats(week.sumOf { it.durationMinutes } / 60.0, week.size, week.sumOf { it.distanceMiles }, airports)
    }

    private fun airportsVisited(completed: List<FlightLogEntity>): List<String> =
        completed.flatMap { listOf(it.originCode, it.destinationCode) }.filter(String::isNotBlank).distinct().sorted()

    private fun unlockedBadges(all: List<FlightLogEntity>, completed: List<FlightLogEntity>, streakDays: Int, zone: ZoneId): Set<String> {
        val clearRun = longestCompletedRun(all)
        val checks = listOf(
            "First Flight" to completed.isNotEmpty(),
            "Ten Flights" to (completed.size >= 10),
            "Red-eye" to completed.any { isRedEye(it, zone) },
            "Transatlantic" to completed.any { it.distanceMiles >= 3000.0 },
            "Airport Collector" to (airportsVisited(completed).size >= 25),
            "Seven-day Streak" to (streakDays >= 7),
            "Clear Skies" to (clearRun >= 5),
        )
        return checks.filter { it.second }.map { it.first }.toSet()
    }

    /** Longest historical run of consecutive completed flights, so a later diversion never revokes the badge. */
    private fun longestCompletedRun(all: List<FlightLogEntity>): Int {
        var best = 0; var run = 0
        all.sortedBy { it.endedAt }.forEach { run = if (it.isCompleted()) run + 1 else 0; best = maxOf(best, run) }
        return best
    }

    private fun isRedEye(flight: FlightLogEntity, zone: ZoneId): Boolean {
        val hour = Instant.ofEpochMilli(flight.startedAt).atZone(zone).hour
        return hour >= 22 || hour < 5
    }

    private fun bestStreak(days: Set<LocalDate>): Int {
        var best = 0; var run = 0; var previous: LocalDate? = null
        days.sorted().forEach { day -> run = if (previous?.plusDays(1) == day) run + 1 else 1; best = maxOf(best, run); previous = day }
        return best
    }
}
