package com.travelblock.app.ui.screen.logbook

data class LogbookUiState(
    val entries: List<LogbookEntryUiModel> = emptyList(),
    val summary: LogbookSummaryUiModel = LogbookSummaryUiModel(),
    val isLoading: Boolean = true,
)

data class LogbookSummaryUiModel(
    val totalFlightsLabel: String = "0",
    val completedFlightsLabel: String = "0",
    val divertedFlightsLabel: String = "0",
    val totalFocusTimeLabel: String = "0m",
    val totalMilesLabel: String = "0 mi",
    val totalPointsLabel: String = "0 pts",
)

data class LogbookEntryUiModel(
    val id: String,
    val flightNumber: String,
    val routeLabel: String,
    val durationLabel: String,
    val distanceLabel: String,
    val statusLabel: String,
    val isCompleted: Boolean,
    val pointsLabel: String,
    val dateLabel: String,
)

data class FlightLogDetailUiState(
    val flight: FlightLogDetailUiModel? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

data class FlightLogDetailUiModel(
    val id: String,
    val flightNumber: String,
    val routeLabel: String,
    val originCode: String,
    val destinationCode: String,
    val statusLabel: String,
    val isCompleted: Boolean,
    val dateLabel: String,
    val durationLabel: String,
    val distanceLabel: String,
    val pointsLabel: String,
    val seatLabel: String,
    val detailMessage: String,
    val focusObjective: String = "",
    val focusTag: String = "",
)
