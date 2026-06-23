package com.travelblock.app.ui.screen.cockpit

import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.ActiveFlightStatus
import com.travelblock.app.domain.model.DiversionSummary
import com.travelblock.app.domain.model.FlightMapModel
import java.time.LocalDateTime

data class CockpitUiState(
    val activeFlight: ActiveFlight? = null,
    val status: ActiveFlightStatus = ActiveFlightStatus.Active,
    val timeRemainingLabel: String = "--:--",
    val elapsedLabel: String = "00:00",
    val progress: Float = 0f,
    val milesFlownLabel: String = "0 mi",
    val milesRemainingLabel: String = "0 mi",
    val totalDistanceLabel: String = "0 mi",
    val flightMap: FlightMapModel? = null,
    val estimatedArrival: LocalDateTime? = null,
    val showDivertDialog: Boolean = false,
    val diversionSummary: DiversionSummary? = null,
) {
    val hasActiveFlight: Boolean = activeFlight != null
}
