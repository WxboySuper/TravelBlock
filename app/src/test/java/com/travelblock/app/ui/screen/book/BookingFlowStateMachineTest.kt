package com.travelblock.app.ui.screen.book

import org.junit.Assert.assertEquals
import org.junit.Test

class BookingFlowStateMachineTest {
    @Test
    fun happyPathTransitionsInOrder() {
        var stage = BookingStage.Manifest
        stage = BookingFlowStateMachine.transition(stage, BookingFlowAction.ContinueToSeatSelection, hasBoardingPass = true)
        stage = BookingFlowStateMachine.transition(stage, BookingFlowAction.ContinueToBoardingPass, hasBoardingPass = true)
        stage = BookingFlowStateMachine.transition(stage, BookingFlowAction.BoardFlight, hasBoardingPass = true)

        assertEquals(BookingStage.GateCall, stage)
    }

    @Test
    fun invalidTransitionsAreIgnored() {
        val stage = BookingFlowStateMachine.transition(
            current = BookingStage.Manifest,
            action = BookingFlowAction.BoardFlight,
            hasBoardingPass = true,
        )

        assertEquals(BookingStage.Manifest, stage)
    }
}
