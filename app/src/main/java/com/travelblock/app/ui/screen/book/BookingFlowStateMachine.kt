package com.travelblock.app.ui.screen.book

enum class BookingStage {
    Manifest,
    SeatSelection,
    BoardingPass,
    GateCall,
}

enum class BookingFlowAction {
    ContinueToSeatSelection,
    ReturnToManifest,
    ContinueToBoardingPass,
    ReturnToReview,
    BoardFlight,
}

object BookingFlowStateMachine {
    fun transition(
        current: BookingStage,
        action: BookingFlowAction,
        hasBoardingPass: Boolean,
    ): BookingStage {
        return when (action) {
            BookingFlowAction.ContinueToSeatSelection -> {
                if (current == BookingStage.Manifest) BookingStage.SeatSelection else current
            }

            BookingFlowAction.ReturnToManifest -> {
                if (current == BookingStage.SeatSelection) BookingStage.Manifest else current
            }

            BookingFlowAction.ContinueToBoardingPass -> {
                if (current == BookingStage.SeatSelection && hasBoardingPass) {
                    BookingStage.BoardingPass
                } else {
                    current
                }
            }

            BookingFlowAction.ReturnToReview -> {
                if (current == BookingStage.BoardingPass) BookingStage.SeatSelection else current
            }

            BookingFlowAction.BoardFlight -> {
                if (current == BookingStage.BoardingPass && hasBoardingPass) {
                    BookingStage.GateCall
                } else {
                    current
                }
            }
        }
    }
}
