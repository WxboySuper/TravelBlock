package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.ActiveFlight
import kotlin.math.floor

object FlightRewardCalculator {
    fun completedReward(
        durationMinutes: Int,
        distanceMiles: Double,
    ): Int {
        val basePoints = durationMinutes * POINTS_PER_MINUTE
        val distanceBonus = floor(distanceMiles / MILES_PER_BONUS_POINT).toInt()
        return basePoints + distanceBonus + COMPLETION_BONUS_POINTS
    }

    fun completedReward(flight: ActiveFlight): Int {
        return completedReward(
            durationMinutes = flight.durationMinutes,
            distanceMiles = flight.distanceMiles,
        )
    }

    fun divertedReward(
        fullReward: Int,
        progress: Double,
    ): Int {
        val clampedProgress = progress.coerceIn(0.0, 1.0)
        if (clampedProgress < MinimumDiversionRewardProgress) return 0
        return floor(fullReward * clampedProgress * DiversionRewardMultiplier).toInt()
    }

    /** About a point for every focused minute: an hour of focus is roughly 60 points, a theme takes a few hours. */
    private const val POINTS_PER_MINUTE = 1
    private const val MILES_PER_BONUS_POINT = 25.0
    private const val COMPLETION_BONUS_POINTS = 5
    private const val MinimumDiversionRewardProgress = 0.25
    private const val DiversionRewardMultiplier = 0.5
}

object ItineraryRewards {
    const val COMPLETION_BONUS = 20

    fun completionBonus(totalFocusMinutes: Int): Int =
        (COMPLETION_BONUS * totalFocusMinutes / 60).coerceAtLeast(COMPLETION_BONUS)
}
