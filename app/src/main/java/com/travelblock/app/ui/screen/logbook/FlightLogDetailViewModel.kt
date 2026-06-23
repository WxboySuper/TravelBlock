package com.travelblock.app.ui.screen.logbook

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelblock.app.data.database.FlightLogEntity
import com.travelblock.app.data.database.FlightLogStatus
import com.travelblock.app.data.repository.FlightLogRepository
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class FlightLogDetailViewModel(
    private val flightId: String,
    private val flightLogRepository: FlightLogRepository,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {
    var uiState by mutableStateOf(FlightLogDetailUiState())
        private set

    init {
        loadFlight()
    }

    private fun loadFlight() {
        viewModelScope.launch {
            val flight = flightLogRepository.getFlightById(flightId)
            uiState = if (flight == null) {
                FlightLogDetailUiState(
                    isLoading = false,
                    errorMessage = "Flight log not found.",
                )
            } else {
                FlightLogDetailUiState(
                    flight = flight.toDetailUiModel(),
                    isLoading = false,
                )
            }
        }
    }

    private fun FlightLogEntity.toDetailUiModel(): FlightLogDetailUiModel {
        val isCompleted = status == FlightLogStatus.COMPLETED.name
        val statusLabel = status.lowercase().replaceFirstChar { it.uppercase() }
        return FlightLogDetailUiModel(
            id = id,
            flightNumber = flightNumber,
            routeLabel = "$originCode -> $destinationCode",
            originCode = originCode,
            destinationCode = destinationCode,
            statusLabel = statusLabel,
            isCompleted = isCompleted,
            dateLabel = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")
                .format(Instant.ofEpochMilli(endedAt).atZone(zoneId)),
            durationLabel = "$durationMinutes min",
            distanceLabel = "${distanceMiles.roundToInt()} mi",
            pointsLabel = "+$pointsEarned pts",
            seatLabel = seat,
            detailMessage = if (isCompleted) {
                "Completed focus flight. Current airport moved to $destinationCode."
            } else {
                "Diverted focus flight. Destination reflects the diversion airport saved for this record."
            },
            focusObjective = focusObjective,
            focusTag = focusTag,
        )
    }
}
