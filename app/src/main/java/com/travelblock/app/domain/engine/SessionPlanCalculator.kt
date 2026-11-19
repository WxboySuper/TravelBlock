package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.SessionPlan
import com.travelblock.app.domain.model.SessionPlanBreakdown
import com.travelblock.app.domain.model.TripPreset

object SessionPlanCalculator {
    fun calculate(plan: SessionPlan): SessionPlanBreakdown? {
        if (!plan.isValid()) return null
        val count = (plan.totalFocusMinutes + plan.blockMinutes - 1) / plan.blockMinutes
        if (count > TripPreset.MAX_TRIP_LEGS) return null
        val base = plan.totalFocusMinutes / count
        val remainder = plan.totalFocusMinutes % count
        val legs = List(count) { index -> base + if (index < remainder) 1 else 0 }
        val breaks = (1 until count).map { completed -> plan.breaks.breakAfter(completed) }
        return SessionPlanBreakdown(legs, breaks)
    }

    private fun SessionPlan.isValid(): Boolean =
        totalFocusMinutes in 1..720 && blockMinutes in 10..180 && breaks.regularMinutes in 5..30 &&
            (breaks.longMinutes == null || breaks.longMinutes in 15..60) &&
            (breaks.longEveryBlocks == null || breaks.longEveryBlocks >= 2)

    private fun com.travelblock.app.domain.model.SessionBreakPlan.breakAfter(completedBlocks: Int): Int =
        if (longEveryBlocks != null && longMinutes != null && completedBlocks % longEveryBlocks == 0) {
            longMinutes
        } else {
            regularMinutes
        }
}
