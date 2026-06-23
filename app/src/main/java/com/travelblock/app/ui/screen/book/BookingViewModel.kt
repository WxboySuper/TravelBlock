package com.travelblock.app.ui.screen.book

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.travelblock.app.domain.model.DraftFlightBooking
import com.travelblock.app.domain.model.SeatOption
import com.travelblock.app.domain.model.TicketClass
import java.time.Clock
import java.time.LocalDateTime
import kotlin.math.roundToInt
import kotlin.random.Random

class BookingViewModel(
    draftFlightBooking: DraftFlightBooking?,
    private val pointsBalance: Int = 0,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {
    var uiState by mutableStateOf(createInitialState(draftFlightBooking))
        private set

    fun selectSeat(seatId: String) {
        val seat = uiState.seatOptions.firstOrNull { it.id == seatId } ?: return
        if (!seat.isAvailable && seat.id != uiState.selectedSeatId) {
            uiState = uiState.copy(seatSelectionMessage = "${seat.label} is already assigned.")
            return
        }
        if (seat.pointsCost > pointsBalance) {
            uiState = uiState.copy(
                seatSelectionMessage = "${seat.label} needs ${seat.pointsCost} pts. " +
                    "You have $pointsBalance pts.",
            )
            return
        }
        val upgradeMessage = if (seat.pointsCost > 0) {
            "${seat.label} held for ${seat.pointsCost} pts. Points are charged in a later confirmation phase."
        } else {
            "Standard seat ${seat.label} selected."
        }
        uiState = uiState.copy(
            selectedSeatId = seatId,
            seatSelectionMessage = upgradeMessage,
        )
    }

    fun updateFocusObjective(objective: String) {
        uiState = uiState.copy(focusObjective = objective.take(MaxObjectiveLength))
    }

    fun selectFocusTag(tag: String) {
        uiState = uiState.copy(focusTag = if (uiState.focusTag == tag) "" else tag)
    }

    fun setPreflightCheck(key: String, checked: Boolean) {
        uiState = when (key) {
            "cabin" -> uiState.copy(cabinSecured = checked)
            "materials" -> uiState.copy(materialsReady = checked)
            "distractions" -> uiState.copy(distractionsStowed = checked)
            else -> uiState
        }
    }

    fun continueToSeatSelection() {
        transition(BookingFlowAction.ContinueToSeatSelection)
    }

    fun returnToManifest() {
        transition(BookingFlowAction.ReturnToManifest)
    }

    fun continueToBoardingPass() {
        transition(BookingFlowAction.ContinueToBoardingPass)
    }

    fun returnToReview() {
        transition(BookingFlowAction.ReturnToReview)
    }

    fun boardFlight() {
        transition(BookingFlowAction.BoardFlight)
    }

    private fun transition(action: BookingFlowAction) {
        val nextStage = BookingFlowStateMachine.transition(
            current = uiState.bookingStage,
            action = action,
            hasBoardingPass = uiState.boardingPass != null,
        )
        uiState = uiState.copy(bookingStage = nextStage)
    }

    private fun createInitialState(draft: DraftFlightBooking?): BookingUiState {
        if (draft == null) return BookingUiState()

        val departureTime = LocalDateTime.now(clock).plusMinutes(8)
        val arrivalTime = departureTime.plusMinutes(draft.durationMinutes.toLong())
        val flightNumber = generateFlightNumber(draft)
        val seatOptions = defaultSeatOptions(
            seed = "$flightNumber-${draft.origin.code}-${draft.destination.code}-${departureTime.toLocalDate()}",
        )

        return BookingUiState(
            draftAvailable = true,
            origin = draft.origin,
            destination = draft.destination,
            durationMinutes = draft.durationMinutes,
            distanceMiles = draft.distanceMiles,
            pointsRewardEstimate = draft.estimatedRewardPoints,
            flightNumber = flightNumber,
            gate = generateGate(draft),
            boardingGroup = generateBoardingGroup(draft),
            departureTime = departureTime,
            arrivalTime = arrivalTime,
            seatOptions = seatOptions,
            selectedSeatId = seatOptions.firstOrNull { it.pointsCost == 0 && it.isAvailable }?.id ?: seatOptions.first().id,
            pointsBalance = pointsBalance,
            maxFlightTimeMinutes = draft.maxFlightTimeMinutes,
            seatSelectionMessage = "Standard seats are free. Upgrade seats require enough points to select.",
        )
    }

    private fun defaultSeatOptions(seed: String): List<SeatOption> {
        val unavailableSeatIds = unavailableSeatsFor(seed)
        val generated = (3..18).flatMap { row ->
            listOf("A", "B", "C", "D", "E", "F").map { column ->
                val seatId = "$row$column"
                val windowSeat = column == "A" || column == "F"
                val aisleSeat = column == "C" || column == "D"
                val ticketClass = when {
                    row <= 5 -> TicketClass.Premium
                    row <= 9 -> TicketClass.ExtraLegroom
                    windowSeat -> TicketClass.Window
                    else -> TicketClass.Standard
                }
                val pointsCost = when (ticketClass) {
                    TicketClass.Standard -> 0
                    TicketClass.Window -> 20
                    TicketClass.ExtraLegroom -> 45
                    TicketClass.Premium -> 80
                }
                val description = when {
                    ticketClass == TicketClass.Premium -> "Forward cabin"
                    ticketClass == TicketClass.ExtraLegroom -> "Extra legroom"
                    windowSeat -> "Window seat"
                    aisleSeat -> "Aisle seat"
                    else -> "Standard seat"
                }
                SeatOption(
                    id = seatId,
                    label = seatId,
                    ticketClass = ticketClass,
                    pointsCost = pointsCost,
                    description = description,
                    isAvailable = seatId !in unavailableSeatIds,
                )
            }
        }
        if (generated.any { it.ticketClass == TicketClass.Standard && it.isAvailable }) return generated

        return generated.map { seat ->
            if (seat.ticketClass == TicketClass.Standard && seat.id == "10B") {
                seat.copy(isAvailable = true)
            } else {
                seat
            }
        }
    }

    private fun unavailableSeatsFor(seed: String): Set<String> {
        val random = Random(stablePositiveHash(seed))
        val unavailable = mutableSetOf<String>()
        val rows = (3..18).toList()
        val columns = listOf("A", "B", "C", "D", "E", "F")
        val clusterStarts = rows.shuffled(random).take(4)

        rows.forEach { row ->
            val cabinPressure = when (row) {
                in 3..5 -> 0.44
                in 6..9 -> 0.32
                in 10..14 -> 0.24
                else -> 0.18
            }
            val rowSwing = random.nextDouble(-0.11, 0.14)
            val clusterPressure = if (clusterStarts.any { row in it..(it + 1) }) 0.18 else 0.0
            val targetUnavailable = ((cabinPressure + rowSwing + clusterPressure) * columns.size)
                .roundToInt()
                .coerceIn(1, 4)
            val weightedColumns = columns.shuffled(random).sortedByDescending { column ->
                val windowWeight = if (column == "A" || column == "F") 0.12 else 0.0
                val groupWeight = if (column in listOf("B", "C", "D", "E") && random.nextBoolean()) 0.08 else 0.0
                random.nextDouble() + windowWeight + groupWeight
            }
            weightedColumns.take(targetUnavailable).forEach { column ->
                unavailable += "$row$column"
            }
        }

        addOccasionalAdjacentPairs(rows, columns, random, unavailable)
        keepEachRowUsable(rows, columns, random, unavailable)
        return unavailable
    }

    private fun addOccasionalAdjacentPairs(
        rows: List<Int>,
        columns: List<String>,
        random: Random,
        unavailable: MutableSet<String>,
    ) {
        repeat(5) {
            val row = rows.random(random)
            val pair = listOf(listOf("A", "B"), listOf("B", "C"), listOf("D", "E"), listOf("E", "F")).random(random)
            if (columns.count { "$row$it" in unavailable } <= 3) {
                pair.forEach { column -> unavailable += "$row$column" }
            }
        }
    }

    private fun keepEachRowUsable(
        rows: List<Int>,
        columns: List<String>,
        random: Random,
        unavailable: MutableSet<String>,
    ) {
        rows.forEach { row ->
            val rowSeats = columns.map { "$row$it" }
            while (rowSeats.count { it !in unavailable } < 2) {
                unavailable.remove(rowSeats.random(random))
            }
            val leftSide = listOf("A", "B", "C").map { "$row$it" }
            val rightSide = listOf("D", "E", "F").map { "$row$it" }
            if (leftSide.all { it in unavailable }) unavailable.remove(leftSide.random(random))
            if (rightSide.all { it in unavailable }) unavailable.remove(rightSide.random(random))
        }
    }

    private fun stablePositiveHash(seed: String): Int {
        return seed.fold(17) { acc, char -> (acc * 31 + char.code) and 0x7fffffff }
    }

    private fun generateFlightNumber(draft: DraftFlightBooking): String {
        val numericSeed = (draft.destination.code.sumOf { it.code } + draft.durationMinutes) % 900 + 100
        return "TB$numericSeed"
    }

    private fun generateGate(draft: DraftFlightBooking): String {
        val letter = draft.destination.code.lastOrNull()?.uppercaseChar() ?: 'A'
        val number = (draft.distanceMiles.roundToInt() % 18) + 1
        return "$letter$number"
    }

    private fun generateBoardingGroup(draft: DraftFlightBooking): String {
        val group = (draft.durationMinutes % 4) + 1
        return "Group $group"
    }

    companion object {
        val FocusTags = listOf("Homework", "Studying", "Coding", "Reading", "Writing", "Project Work", "Chores/Admin", "Other")
        private const val MaxObjectiveLength = 120
    }
}
