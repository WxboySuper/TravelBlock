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
import com.travelblock.app.data.settings.AirportCode
import com.travelblock.app.data.settings.UserSettings
import com.travelblock.app.domain.engine.ActiveFlightProgress
import com.travelblock.app.domain.engine.ActiveFlightSnapshot
import com.travelblock.app.domain.engine.DiversionEngine
import com.travelblock.app.domain.engine.FlightRouteInterpolation
import com.travelblock.app.domain.engine.FlightRewardCalculator
import com.travelblock.app.domain.engine.ItineraryRewards
import com.travelblock.app.domain.engine.AirportRouteEngine
import com.travelblock.app.domain.engine.toClockLabel
import com.travelblock.app.domain.engine.toMilesLabel
import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.ActiveFlightStatus
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.DiversionSummary
import com.travelblock.app.domain.model.FlightItinerary
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
    private val onPrepareForLanding: () -> Unit = {},
    autoTick: Boolean = false,
) : ViewModel() {
    var uiState by mutableStateOf(activeFlight.toUiState())
        private set
    private var resultSaved = false
    private var tickerJob: Job? = null
    private var landingAnnouncementSent = false

    init {
        if (autoTick) {
            startTicker()
        }
    }

    private fun startTicker() {
        if (uiState.activeFlight == null || uiState.status !in setOf(ActiveFlightStatus.Active, ActiveFlightStatus.Layover)) return
        if (tickerJob?.isActive == true) return
        tickerJob = viewModelScope.launch {
            while (isActive && uiState.activeFlight != null && uiState.status in setOf(ActiveFlightStatus.Active, ActiveFlightStatus.Layover)) {
                tick()
                delay(1_000)
            }
        }
    }

    fun tick() {
        val flight = uiState.activeFlight ?: return
        when (uiState.status) {
            ActiveFlightStatus.Arrived -> Unit
            ActiveFlightStatus.Layover -> updateLayover(flight)
            ActiveFlightStatus.Active -> tickActiveFlight(flight)
            else -> Unit
        }
    }

    private fun updateLayover(flight: ActiveFlight) {
        uiState = flight.toUiState(status = ActiveFlightStatus.Layover)
    }

    private fun tickActiveFlight(flight: ActiveFlight) {
        val snapshot = ActiveFlightProgress.snapshot(flight, clock.instant())
        if (!landingAnnouncementSent && snapshot.remaining <= Duration.ofMinutes(2)) {
            landingAnnouncementSent = true
            onPrepareForLanding()
        }
        val nextStatus = snapshot.toFlightStatus(flight)
        if (nextStatus == ActiveFlightStatus.Layover) {
            beginLayover(flight)
            return
        }
        uiState = flight.toUiState(status = nextStatus)
        if (nextStatus == ActiveFlightStatus.Arrived) saveCompletedArrival(flight)
    }

    private fun ActiveFlightSnapshot.toFlightStatus(flight: ActiveFlight): ActiveFlightStatus = when {
        !isComplete -> ActiveFlightStatus.Active
        flight.isItinerary && !flight.isLastItineraryLeg -> ActiveFlightStatus.Layover
        else -> ActiveFlightStatus.Arrived
    }

    private fun saveCompletedArrival(flight: ActiveFlight) {
        val bonus = if (flight.isItinerary) ItineraryRewards.COMPLETION_BONUS else 0
        saveResult(
            FlightResultRequest(
                flight = flight,
                status = FlightLogStatus.COMPLETED,
                pointsEarned = FlightRewardCalculator.completedReward(flight),
                tripBonus = bonus,
            ),
        )
    }

    private fun beginLayover(flight: ActiveFlight) {
        val earned = FlightRewardCalculator.completedReward(flight)
        val breakMinutes = FlightItinerary.layoverMinutesAfter(flight.itineraryLegIndex + 1)
        val layoverFlight = flight.copy(
            status = ActiveFlightStatus.Layover,
            itineraryPointsEarned = flight.itineraryPointsEarned + earned,
            layoverEndsAtEpochMillis = clock.millis() + Duration.ofMinutes(breakMinutes.toLong()).toMillis(),
        )
        uiState = layoverFlight.toUiState(ActiveFlightStatus.Layover)
        saveResult(
            FlightResultRequest(
                flight = flight,
                status = FlightLogStatus.COMPLETED,
                pointsEarned = earned,
                sessionAfter = layoverFlight,
            ),
        )
    }

    fun skipLayover() {
        val flight = uiState.activeFlight ?: return
        if (uiState.status != ActiveFlightStatus.Layover) return
        val skipped = flight.copy(layoverEndsAtEpochMillis = clock.millis())
        uiState = skipped.toUiState(ActiveFlightStatus.Layover)
        viewModelScope.launch { activeFlightSessionRepository?.saveActiveFlight(skipped) }
    }

    fun boardNextLeg() {
        val flight = uiState.activeFlight ?: return
        val endsAt = flight.layoverEndsAtEpochMillis ?: return
        if (uiState.status != ActiveFlightStatus.Layover || clock.millis() < endsAt) return
        viewModelScope.launch {
            val settings = settingsRepository?.getSettings() ?: UserSettings()
            val nextFlight = createNextLeg(flight, settings) ?: return@launch
            resultSaved = false
            landingAnnouncementSent = false
            uiState = nextFlight.toUiState()
            activeFlightSessionRepository?.saveActiveFlight(nextFlight)
        }
    }

    private fun createNextLeg(flight: ActiveFlight, settings: UserSettings): ActiveFlight? {
        val codes = flight.itineraryCodes?.split(',') ?: return null
        val index = flight.itineraryLegIndex + 1
        val origin = airportFor(codes.getOrNull(index)) ?: return null
        val destination = airportFor(codes.getOrNull(index + 1)) ?: return null
        val startedAt = clock.instant()
        val durationSeconds = if (settings.debugShortFlightsEnabled && com.travelblock.app.BuildConfig.DEBUG) {
            settings.debugFlightDurationSeconds.toLong()
        } else {
            flight.durationMinutes * 60L
        }
        val arrival = startedAt.plusSeconds(durationSeconds)
        return flight.copy(
            origin = origin,
            destination = destination,
            distanceMiles = AirportRouteEngine().distanceMiles(origin, destination),
            flightNumber = "${flight.flightNumber}L${index + 1}",
            startedAt = startedAt,
            plannedArrivalAt = arrival,
            itineraryLegIndex = index,
            layoverEndsAtEpochMillis = null,
            status = ActiveFlightStatus.Active,
        )
    }

    private fun airportFor(code: String?): Airport? = airports.firstOrNull { it.code == code }

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
            FlightResultRequest(
                flight = flight,
                status = FlightLogStatus.DIVERTED,
                pointsEarned = pointsEarned,
                focusedDuration = snapshot.elapsed,
                landedAirport = diversionSummary.diversionAirport,
            ),
        )
    }

    private fun saveResult(result: FlightResultRequest) {
        if (flightLogRepository == null) return
        if (pointsRepository == null) return
        if (settingsRepository == null) return
        if (resultSaved) return
        resultSaved = true
        viewModelScope.launch { persistResult(result) }
    }

    private suspend fun persistResult(result: FlightResultRequest) {
        val flight = result.flight
        val status = result.status
        val flightLogs = flightLogRepository ?: return
        val points = pointsRepository ?: return
        val settings = settingsRepository ?: return
        val resolvedPointsEarned = result.pointsEarned ?: when (status) {
            FlightLogStatus.COMPLETED -> FlightRewardCalculator.completedReward(flight)
            FlightLogStatus.DIVERTED -> 0
        }
        val flightId = UUID.nameUUIDFromBytes(
            "${flight.flightNumber}:${flight.startedAt.toEpochMilli()}".toByteArray(),
        ).toString()
        val endedAt = clock.instant()
        flightLogs.insertFlightLog(buildFlightLog(result, flightId, resolvedPointsEarned, endedAt.toEpochMilli()))
        points.insertTransaction(buildFlightPoints(result, flightId, resolvedPointsEarned, endedAt.toEpochMilli()))
        saveTripBonus(points, result, flightId, endedAt.toEpochMilli())
        saveArrivalAirport(settings, result, flight)
        saveSessionAfterResult(result.sessionAfter)
    }

    private fun buildFlightLog(
        result: FlightResultRequest,
        flightId: String,
        pointsEarned: Int,
        endedAtMillis: Long,
    ): FlightLogEntity {
        val flight = result.flight
        val duration = result.focusedDuration?.toMinutes()?.toInt() ?: flight.durationMinutes
        return FlightLogEntity(
            id = flightId,
            flightNumber = flight.flightNumber,
            originCode = flight.origin.code,
            destinationCode = result.landedAirport?.code ?: flight.destination.code,
            durationMinutes = duration,
            distanceMiles = flight.distanceMiles,
            status = result.status.name,
            startedAt = flight.startedAt.toEpochMilli(),
            endedAt = endedAtMillis,
            seat = flight.seat.label,
            pointsEarned = pointsEarned,
            focusObjective = flight.focusObjective,
            focusTag = flight.focusTag,
            todoistTaskId = flight.todoistTaskId,
            todoistTaskContent = flight.todoistTaskContent,
        )
    }

    private suspend fun buildFlightPoints(
        result: FlightResultRequest,
        flightId: String,
        pointsEarned: Int,
        endedAtMillis: Long,
    ) = PointsTransactionEntity(
        id = flightId,
        amount = pointsEarned,
        reason = if (result.status == FlightLogStatus.COMPLETED) "Completed flight" else "Diverted flight",
        createdAt = endedAtMillis,
        relatedFlightId = flightId,
    )

    private suspend fun saveTripBonus(
        points: PointsRepository,
        result: FlightResultRequest,
        flightId: String,
        endedAtMillis: Long,
    ) {
        if (result.tripBonus <= 0) return
        points.insertTransaction(
            PointsTransactionEntity(
                id = "$flightId-trip-bonus",
                amount = result.tripBonus,
                reason = "Completed long-haul trip",
                createdAt = endedAtMillis,
                relatedFlightId = flightId,
            ),
        )
    }

    private suspend fun saveArrivalAirport(
        settings: SettingsRepository,
        result: FlightResultRequest,
        flight: ActiveFlight,
    ) {
        if (result.status !in setOf(FlightLogStatus.COMPLETED, FlightLogStatus.DIVERTED)) return
        settings.setCurrentAirportCode(AirportCode(result.landedAirport?.code ?: flight.destination.code))
    }

    private suspend fun saveSessionAfterResult(sessionAfter: ActiveFlight?) {
        if (sessionAfter != null) activeFlightSessionRepository?.saveActiveFlight(sessionAfter)
        else activeFlightSessionRepository?.clearActiveFlight()
    }

    private fun ActiveFlight?.toUiState(
        status: ActiveFlightStatus = this?.status ?: ActiveFlightStatus.Active,
    ): CockpitUiState {
        if (this == null) return CockpitUiState(activeFlight = null)

        val snapshot = ActiveFlightProgress.snapshot(this, clock.instant())
        val resolvedStatus = when {
            status == ActiveFlightStatus.Diverted -> ActiveFlightStatus.Diverted
            status == ActiveFlightStatus.Layover -> ActiveFlightStatus.Layover
            else -> status
        }

        return CockpitUiState(
            activeFlight = this.copy(status = resolvedStatus),
            status = resolvedStatus,
            timeRemainingLabel = if (resolvedStatus == ActiveFlightStatus.Layover) {
                Duration.ofMillis(((layoverEndsAtEpochMillis ?: clock.millis()) - clock.millis()).coerceAtLeast(0L)).toClockLabel()
            } else snapshot.remaining.toClockLabel(),
            elapsedLabel = snapshot.elapsed.toClockLabel(),
            progress = if (resolvedStatus == ActiveFlightStatus.Layover) 0f else snapshot.progress.toFloat(),
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

private data class FlightResultRequest(
    val flight: ActiveFlight,
    val status: FlightLogStatus,
    val pointsEarned: Int? = null,
    val focusedDuration: Duration? = null,
    val landedAirport: Airport? = null,
    val tripBonus: Int = 0,
    val sessionAfter: ActiveFlight? = null,
)
