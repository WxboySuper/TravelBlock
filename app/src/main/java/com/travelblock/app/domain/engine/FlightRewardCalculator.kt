package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.ActiveFlight
import kotlin.math.floor

object FlightRewardCalculator {
    fun completedReward(
        durationMinutes: Int,
        distanceMiles: Double,
    ): Int {
        val basePoints = durationMinutes * 4
        val distanceBonus = floor(distanceMiles / 10.0).toInt()
        val completionBonus = 25
        return basePoints + distanceBonus + completionBonus
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

    private const val MinimumDiversionRewardProgress = 0.25
    private const val DiversionRewardMultiplier = 0.5
}
