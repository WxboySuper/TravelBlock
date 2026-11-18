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
import com.travelblock.app.domain.engine.PilotProgressionEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.roundToInt

class LogbookViewModel(
    private val flightLogRepository: FlightLogRepository,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {
    var uiState by mutableStateOf(LogbookUiState())
        private set

    init {
        viewModelScope.launch {
            combine(flightLogRepository.observeFlightLogs(), localDateTicker()) { entries, _ -> entries }.collect { entries ->
                val sortedEntries = entries.sortedByDescending { it.endedAt }
                uiState = LogbookUiState(
                    entries = sortedEntries.map { it.toUiModel() },
                    summary = sortedEntries.toSummaryUiModel(),
                    pilot = PilotProgressionEngine.summarize(sortedEntries, zoneId),
                    isLoading = false,
                )
            }
        }
    }

    /** Emits the local date, re-checking each minute, so streak and weekly stats refresh across midnight. */
    private fun localDateTicker(): Flow<LocalDate> = flow {
        while (true) {
            emit(LocalDate.now(zoneId))
            delay(60_000)
        }
    }.distinctUntilChanged()

    private fun FlightLogEntity.toUiModel(): LogbookEntryUiModel {
        return LogbookEntryUiModel(
            id = id,
            flightNumber = flightNumber,
            routeLabel = "$originCode -> $destinationCode",
            durationLabel = "$durationMinutes min",
            distanceLabel = "${distanceMiles.roundToInt()} mi",
            statusLabel = status.lowercase().replaceFirstChar { it.uppercase() },
            isCompleted = status == FlightLogStatus.COMPLETED.name,
            pointsLabel = "+$pointsEarned pts",
            dateLabel = DateTimeFormatter.ofPattern("MMM d, h:mm a")
                .format(Instant.ofEpochMilli(endedAt).atZone(zoneId)),
        )
    }

    private fun List<FlightLogEntity>.toSummaryUiModel(): LogbookSummaryUiModel {
        val completed = count { it.status == FlightLogStatus.COMPLETED.name }
        val diverted = count { it.status == FlightLogStatus.DIVERTED.name }
        val totalMinutes = sumOf { it.durationMinutes }
        val totalMiles = sumOf { it.distanceMiles }
        val totalPoints = sumOf { it.pointsEarned }

        return LogbookSummaryUiModel(
            totalFlightsLabel = size.toString(),
            completedFlightsLabel = completed.toString(),
            divertedFlightsLabel = diverted.toString(),
            totalFocusTimeLabel = totalMinutes.toFocusTimeLabel(),
            totalMilesLabel = "${totalMiles.roundToInt()} mi",
            totalPointsLabel = "$totalPoints pts",
        )
    }

    private fun Int.toFocusTimeLabel(): String {
        val hours = this / 60
        val minutes = this % 60
        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }
}
