package com.travelblock.app.ui.screen.book

import com.travelblock.app.domain.engine.HubConnection
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.SessionBreakPlan
import com.travelblock.app.domain.model.SessionPlan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TripOverviewTest {
    private val a = airport("AAA")
    private val b = airport("BBB")
    private val c = airport("CCC")

    @Test
    fun `a planned session shows each flight with the break after it`() {
        val plan = SessionPlan(totalFocusMinutes = 100, blockMinutes = 50, breaks = SessionBreakPlan())

        val legs = tripOverviewLegs(listOf(a, b, c), plan, null)

        assertEquals(listOf("AAA", "BBB"), legs.map { it.from.code to it.to.code }.first().toList())
        assertEquals(2, legs.size)
        assertEquals(100, legs.sumOf { it.minutes })
        assertEquals(SessionBreakPlan().regularMinutes, legs.first().breakAfterMinutes)
        assertNull(legs.last().breakAfterMinutes)
    }

    @Test
    fun `a hub connection shows its two flights and the layover`() {
        val hub = HubConnection(hub = b, firstLegMinutes = 12, secondLegMinutes = 30, layoverMinutes = 5, directMinutes = 35)

        val legs = tripOverviewLegs(listOf(a, b, c), null, hub)

        assertEquals(listOf(12, 30), legs.map { it.minutes })
        assertEquals(5, legs.first().breakAfterMinutes)
    }

    @Test
    fun `without a plan or a hub there is nothing to lay out`() {
        assertEquals(emptyList<OverviewLeg>(), tripOverviewLegs(listOf(a, b), null, null))
    }

    private fun airport(code: String) = Airport(code, code, code, code, code, null, "US", 0.0, 0.0, null)
}
