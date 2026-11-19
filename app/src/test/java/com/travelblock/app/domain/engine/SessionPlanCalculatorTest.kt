package com.travelblock.app.domain.engine

import com.travelblock.app.domain.model.SessionPlan
import com.travelblock.app.domain.model.SessionBreakPlan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionPlanCalculatorTest {
    @Test
    fun `four hours with fifty minute blocks distributes focus evenly`() {
        val result = SessionPlanCalculator.calculate(SessionPlan(240, 50, SessionBreakPlan(10, 20, 4)))

        assertEquals(listOf(48, 48, 48, 48, 48), result?.legDurationsMinutes)
        assertEquals(listOf(10, 10, 10, 20), result?.breakDurationsMinutes)
        assertEquals(240, result?.totalFocusMinutes)
        assertEquals(50, result?.totalBreakMinutes)
    }

    @Test
    fun `planner rejects a plan that exceeds twenty four legs`() {
        assertNull(SessionPlanCalculator.calculate(SessionPlan(720, 10, SessionBreakPlan(5))))
    }
}
