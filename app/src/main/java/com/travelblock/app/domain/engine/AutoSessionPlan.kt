package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.SessionBreakPlan
import com.travelblock.app.domain.model.SessionPlan
import com.travelblock.app.domain.model.TripPreset

/**
 * Turns a long total travel time into a long haul by itself: anything beyond a single comfortable focus block is
 * split into flights of about [PREFERRED_BLOCK_MINUTES] with the standard breaks between them.
 */
object AutoSessionPlan {
    const val SINGLE_FLIGHT_MAX_MINUTES = 90
    private const val PREFERRED_BLOCK_MINUTES = 50

    fun forTotal(totalMinutes: Int): SessionPlan? {
        if (totalMinutes <= SINGLE_FLIGHT_MAX_MINUTES) return null
        val block = blockFor(totalMinutes)
        val plan = SessionPlan(totalMinutes, block, SessionBreakPlan())
        return plan.takeIf { SessionPlanCalculator.calculate(it) != null }
    }

    /** The flight count and the minutes of breaks a total would produce, or null for a single flight. */
    fun summary(totalMinutes: Int): AutoPlanSummary? {
        val breakdown = forTotal(totalMinutes)?.let(SessionPlanCalculator::calculate) ?: return null
        return AutoPlanSummary(breakdown.legCount, breakdown.totalBreakMinutes)
    }

    private fun blockFor(totalMinutes: Int): Int {
        val legs = (totalMinutes + PREFERRED_BLOCK_MINUTES - 1) / PREFERRED_BLOCK_MINUTES
        return if (legs > TripPreset.MAX_TRIP_LEGS) (totalMinutes + TripPreset.MAX_TRIP_LEGS - 1) / TripPreset.MAX_TRIP_LEGS else PREFERRED_BLOCK_MINUTES
    }
}

data class AutoPlanSummary(val legCount: Int, val breakMinutes: Int)
