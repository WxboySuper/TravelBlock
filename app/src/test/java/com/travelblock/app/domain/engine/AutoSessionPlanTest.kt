package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.TripPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoSessionPlanTest {
    @Test
    fun anythingUpToANinetyMinuteBlockStaysASingleFlight() {
        assertNull(AutoSessionPlan.forTotal(25))
        assertNull(AutoSessionPlan.forTotal(AutoSessionPlan.SINGLE_FLIGHT_MAX_MINUTES))
        assertNull(AutoSessionPlan.summary(60))
    }

    @Test
    fun longerTravelIsSplitIntoFlightsWithBreaks() {
        val summary = AutoSessionPlan.summary(240)
        assertNotNull(summary)
        assertEquals(5, summary!!.legCount)
        assertEquals(4 * 10 + 10, summary.breakMinutes)
    }

    @Test
    fun theLongestTripStillFitsTheLegLimit() {
        val plan = AutoSessionPlan.forTotal(720)
        assertNotNull(plan)
        val breakdown = SessionPlanCalculator.calculate(plan!!)
        assertTrue(breakdown!!.legCount <= TripPreset.MAX_TRIP_LEGS)
        assertEquals(720, breakdown.totalFocusMinutes)
    }
}
