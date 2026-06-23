package com.travelblock.app.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class FlightRewardCalculatorTest {
    @Test
    fun earlyDiversionEarnsNoPoints() {
        assertEquals(
            0,
            FlightRewardCalculator.divertedReward(
                fullReward = 151,
                progress = 0.20,
            ),
        )
    }

    @Test
    fun laterDiversionEarnsReducedPoints() {
        assertEquals(
            37,
            FlightRewardCalculator.divertedReward(
                fullReward = 151,
                progress = 0.50,
            ),
        )
    }

    @Test
    fun diversionRewardProgressIsClamped() {
        assertEquals(
            75,
            FlightRewardCalculator.divertedReward(
                fullReward = 151,
                progress = 1.40,
            ),
        )
    }
}
