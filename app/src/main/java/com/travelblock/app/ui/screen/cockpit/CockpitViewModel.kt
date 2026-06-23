package com.travelblock.app.ui.screen.cockpit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelblock.app.data.database.FlightLogEntity
import com.travelblock.app.data.database.FlightLogStatus
import com.travelblock.app.data.database.PointsTransactionEntity
import com.travelblock.app.data.repository.ActiveFlightSessionRepository
import com.travelblock.app.data.repository.FlightLogRepository
import com.travelblock.app.data.repository.PointsRepository
import com.travelblock.app.data.settings.SettingsRepository
import com.travelblock.app.domain.engine.ActiveFlightProgress
import com.travelblock.app.domain.engine.DiversionEngine
import com.travelblock.app.domain.engine.FlightRouteInterpolation
import com.travelblock.app.domain.engine.FlightRewardCalculator
import com.travelblock.app.domain.engine.toClockLabel
import com.travelblock.app.domain.engine.toMilesLabel
import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.ActiveFlightStatus
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.DiversionSummary
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CockpitViewModel(
    activeFlight: ActiveFlight?,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val zoneId: ZoneId = ZoneId.systemDefault(),
    private val flightLogRepository: FlightLogRepository? = null,
    private val pointsRepository: PointsRepository? = null,
    private val settingsRepository: SettingsRepository? = null,
    private val activeFlightSessionRepository: ActiveFlightSessionRepository? = null,
    private val airports: List<Airport> = emptyList(),
    autoTick: Boolean = false,
) : ViewModel() {
    var uiState by mutableStateOf(activeFlight.toUiState())
        private set
    private var resultSaved = false
    private var tickerJob: Job? = null

    init {
        if (autoTick) {
            startTicker()
        }
    }

    private fun startTicker() {
        if (uiState.activeFlight == null || uiState.status != ActiveFlightStatus.Active) return
        if (tickerJob?.isActive == true) return
        tickerJob = viewModelScope.launch {
            while (isActive && uiState.activeFlight != null && uiState.status == ActiveFlightStatus.Active) {
                tick()
                delay(1_000)
            }
        }
    }

    fun tick() {
        val flight = uiState.activeFlight ?: return
        if (uiState.status == ActiveFlightStatus.Arrived) {
            saveResult(flight, FlightLogStatus.COMPLETED)
            return
        }
        if (uiState.status != ActiveFlightStatus.Active) return

        val snapshot = ActiveFlightProgress.snapshot(flight, clock.instant())
        val nextStatus = if (snapshot.isComplete) ActiveFlightStatus.Arrived else ActiveFlightStatus.Active
        uiState = flight.toUiState(status = nextStatus)
        if (nextStatus == ActiveFlightStatus.Arrived) {
            saveResult(flight, FlightLogStatus.COMPLETED)
        }
    }

    fun requestDivert() {
        if (uiState.activeFlight == null || uiState.status != ActiveFlightStatus.Active) return
        uiState = uiState.copy(showDivertDialog = true)
    }

    fun dismissDivertDialog() {
        uiState = uiState.copy(showDivertDialog = false)
    }

    fun confirmDivert() {
        val flight = uiState.activeFlight ?: return
        val snapshot = ActiveFlightProgress.snapshot(flight, clock.instant())
        val pointsEarned = FlightRewardCalculator.divertedReward(
            fullReward = FlightRewardCalculator.completedReward(flight),
            progress = snapshot.progress,
        )
        val diversionAirport = DiversionEngine.findClosestDiversionAirport(
            flight = flight,
            airports = airports.ifEmpty { listOf(flight.origin) },
            progress = snapshot.progress,
        )
        val diversionSummary = DiversionSummary.fromFlight(
            flight = flight,
            snapshot = snapshot,
            diversionAirport = diversionAirport?.airport ?: flight.origin,
            distanceToDiversionAirportMiles = diversionAirport?.distanceMiles ?: 0.0,
            pointsEarned = pointsEarned,
        )
        uiState = flight.toUiState(status = ActiveFlightStatus.Diverted)
            .copy(
                showDivertDialog = false,
                diversionSummary = diversionSummary,
            )
        saveResult(
            flight = flight,
            status = FlightLogStatus.DIVERTED,
            pointsEarned = pointsEarned,
            focusedDuration = snapshot.elapsed,
            landedAirport = diversionSummary.diversionAirport,
        )
    }

    private fun saveResult(
        flight: ActiveFlight,
        status: FlightLogStatus,
        pointsEarned: Int? = null,
        focusedDuration: Duration? = null,
        landedAirport: Airport = flight.destination,
    ) {
        val flightLogRepository = flightLogRepository ?: return
        val pointsRepository = pointsRepository ?: return
        val settingsRepository = settingsRepository ?: return
        if (resultSaved) return
        resultSaved = true

        val resolvedPointsEarned = pointsEarned ?: when (status) {
            FlightLogStatus.COMPLETED -> FlightRewardCalculator.completedReward(flight)
            FlightLogStatus.DIVERTED -> 0
        }
        val loggedDurationMinutes = when (status) {
            FlightLogStatus.COMPLETED -> flight.durationMinutes
            FlightLogStatus.DIVERTED -> focusedDuration?.toMinutes()?.toInt() ?: 0
        }
        val flightId = UUID.randomUUID().toString()
        val endedAt = clock.instant()

        viewModelScope.launch {
            flightLogRepository.insertFlightLog(
                FlightLogEntity(
                    id = flightId,
                    flightNumber = flight.flightNumber,
                    originCode = flight.origin.code,
                    destinationCode = landedAirport.code,
                    durationMinutes = loggedDurationMinutes,
                    distanceMiles = flight.distanceMiles,
                    status = status.name,
                    startedAt = flight.startedAt.toEpochMilli(),
                    endedAt = endedAt.toEpochMilli(),
                    seat = flight.seat.label,
                    pointsEarned = resolvedPointsEarned,
                    focusObjective = flight.focusObjective,
                    focusTag = flight.focusTag,
                ),
            )
            pointsRepository.insertTransaction(
                PointsTransactionEntity(
                    id = UUID.randomUUID().toString(),
                    amount = resolvedPointsEarned,
                    reason = if (status == FlightLogStatus.COMPLETED) "Completed flight" else "Diverted flight",
                    createdAt = endedAt.toEpochMilli(),
                    relatedFlightId = flightId,
                ),
            )
            if (status == FlightLogStatus.COMPLETED || status == FlightLogStatus.DIVERTED) {
                settingsRepository.setCurrentAirportCode(landedAirport.code)
            }
            activeFlightSessionRepository?.clearActiveFlight()
        }
    }

    private fun ActiveFlight?.toUiState(
        status: ActiveFlightStatus = this?.status ?: ActiveFlightStatus.Active,
    ): CockpitUiState {
        if (this == null) return CockpitUiState(activeFlight = null)

        val snapshot = ActiveFlightProgress.snapshot(this, clock.instant())
        val resolvedStatus = when {
            status == ActiveFlightStatus.Diverted -> ActiveFlightStatus.Diverted
            snapshot.isComplete -> ActiveFlightStatus.Arrived
            else -> status
        }

        return CockpitUiState(
            activeFlight = this.copy(status = resolvedStatus),
            status = resolvedStatus,
            timeRemainingLabel = snapshot.remaining.toClockLabel(),
            elapsedLabel = snapshot.elapsed.toClockLabel(),
            progress = snapshot.progress.toFloat(),
            milesFlownLabel = snapshot.milesFlown.toMilesLabel(),
            milesRemainingLabel = snapshot.milesRemaining.toMilesLabel(),
            totalDistanceLabel = distanceMiles.toMilesLabel(),
            flightMap = FlightRouteInterpolation.mapModelForFlight(
                activeFlight = this,
                progress = snapshot.progress,
            ),
            estimatedArrival = LocalDateTime.ofInstant(plannedArrivalAt, zoneId),
        )
    }
}
