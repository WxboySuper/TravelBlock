package com.travelblock.app.domain.model

data class SessionPlan(
    val totalFocusMinutes: Int,
    val blockMinutes: Int,
    val breaks: SessionBreakPlan = SessionBreakPlan(),
)

data class SessionBreakPlan(
    val regularMinutes: Int = 10,
    val longMinutes: Int? = 20,
    val longEveryBlocks: Int? = 4,
)

data class SessionPlanBreakdown(
    val legDurationsMinutes: List<Int>,
    val breakDurationsMinutes: List<Int>,
) {
    val totalFocusMinutes: Int get() = legDurationsMinutes.sum()
    val totalBreakMinutes: Int get() = breakDurationsMinutes.sum()
    val legCount: Int get() = legDurationsMinutes.size
}
