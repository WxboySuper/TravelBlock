package com.travelblock.app.ui
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.travelblock.app.TravelBlockContainer
import com.travelblock.app.data.database.FlightLogEntity
import com.travelblock.app.data.database.FlightLogStatus
import com.travelblock.app.data.database.PointsTransactionEntity
import com.travelblock.app.data.settings.UserSettings
import com.travelblock.app.data.settings.AirportCode
import com.travelblock.app.data.settings.SettingEnabled
import com.travelblock.app.domain.engine.FlightRewardCalculator
import com.travelblock.app.domain.engine.createInitialFlight
import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.ArrivalSummary
import com.travelblock.app.domain.model.BoardingPass
import com.travelblock.app.domain.model.DiversionSummary
import com.travelblock.app.domain.model.DraftFlightBooking
import com.travelblock.app.ui.navigation.TravelBlockBottomBar
import com.travelblock.app.ui.navigation.TravelBlockDestination
import com.travelblock.app.ui.screen.arrival.ArrivalScreen
import com.travelblock.app.ui.screen.arrival.ArrivalScreenActions
import com.travelblock.app.ui.screen.diversion.DiversionScreenActions
import com.travelblock.app.ui.screen.book.BookScreen
import com.travelblock.app.ui.screen.book.QuickBoardingScreen
import com.travelblock.app.ui.screen.book.createQuickBoardingPass
import com.travelblock.app.ui.screen.cockpit.CockpitScreen
import com.travelblock.app.ui.screen.cockpit.CockpitActions
import com.travelblock.app.ui.screen.diversion.DiversionScreen
import com.travelblock.app.ui.screen.home.HomeScreen
import com.travelblock.app.ui.screen.logbook.FlightLogDetailScreen
import com.travelblock.app.ui.screen.logbook.LogbookScreen
import com.travelblock.app.ui.screen.settings.SettingsScreen
import com.travelblock.app.ui.screen.onboarding.OnboardingScreen
import com.travelblock.app.ui.screen.store.StoreScreen
import com.travelblock.app.ui.util.FlightAudioCue
import com.travelblock.app.ui.util.FlightAudioCuePlayer
import com.travelblock.app.ui.util.AnnouncementDetails
import com.travelblock.app.ui.util.announcement
import com.travelblock.app.ui.util.AudioPlaybackSettings
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.travelblock.app.MainActivity
import com.travelblock.app.ui.theme.TravelBlockTheme

private suspend fun recordRestoredArrival(
    container: TravelBlockContainer,
    flight: ActiveFlight,
) {
    val legPoints = FlightRewardCalculator.completedReward(flight)
    val bonus = if (flight.isItinerary) {
        com.travelblock.app.domain.engine.ItineraryRewards.completionBonus(flight.itineraryFocusMinutes)
    } else 0
    recordRestoredLeg(container, flight, legPoints, bonus)
}

private suspend fun recordRestoredLeg(
    container: TravelBlockContainer,
    flight: ActiveFlight,
    pointsEarned: Int,
    tripBonus: Int = 0,
) {
    val flightId = UUID.nameUUIDFromBytes(
        "${flight.flightNumber}:${flight.startedAt.toEpochMilli()}".toByteArray(),
    ).toString()
    val endedAt = flight.plannedArrivalAt.toEpochMilli()
    container.flightLogRepository.insertFlightLog(
        FlightLogEntity(
            id = flightId,
            flightNumber = flight.flightNumber,
            originCode = flight.origin.code,
            destinationCode = flight.destination.code,
            durationMinutes = flight.durationMinutes,
            distanceMiles = flight.distanceMiles,
            status = FlightLogStatus.COMPLETED.name,
            startedAt = flight.startedAt.toEpochMilli(),
            endedAt = endedAt,
            seat = flight.seat.label,
            pointsEarned = pointsEarned,
            focusObjective = flight.focusObjective,
            focusTag = flight.focusTag,
            todoistTaskId = flight.todoistTaskId,
            todoistTaskContent = flight.todoistTaskContent,
        ),
    )
    container.pointsRepository.insertTransaction(
        PointsTransactionEntity(
            id = flightId,
            amount = pointsEarned,
            reason = "Completed flight",
            createdAt = endedAt,
            relatedFlightId = flightId,
        ),
    )
    if (tripBonus > 0) {
        container.pointsRepository.insertTransaction(
            PointsTransactionEntity(
                id = "$flightId-trip-bonus",
                amount = tripBonus,
                reason = "Completed long-haul trip",
                createdAt = endedAt,
                relatedFlightId = flightId,
            ),
        )
    }
    container.settingsRepository.setCurrentAirportCode(AirportCode(flight.destination.code))
}

@Composable
internal fun RestoreActiveFlightEffect(runtime: TravelBlockRuntime) {
    val state = runtime.state
    LaunchedEffect(state.activeFlightRestored) {
        if (state.activeFlightRestored) return@LaunchedEffect
        val flight = runtime.container.activeFlightSessionRepository.getActiveFlight()
        restoreFlightSession(flight, runtime)
    }
}

private suspend fun restoreFlightSession(flight: ActiveFlight?, runtime: TravelBlockRuntime) {
    if (flight == null) {
        runtime.state.restore(null, null)
        return
    }
    val now = Instant.now()
    if (flight.status == com.travelblock.app.domain.model.ActiveFlightStatus.Layover) {
        restoreLayover(flight, runtime)
    } else if (shouldRestoreAsArrival(flight, now) && flight.isItinerary && !flight.isLastItineraryLeg) {
        restoreLandedItineraryLeg(flight, runtime)
    } else if (shouldRestoreAsArrival(flight, now)) {
        restoreArrivedFlight(flight, runtime)
    } else {
        runtime.container.landingAlarmScheduler.schedule(flight)
        runtime.state.restore(flight, null)
    }
}

private suspend fun restoreLayover(flight: ActiveFlight, runtime: TravelBlockRuntime) {
    runtime.container.landingAlarmScheduler.schedule(flight)
    runtime.container.liveFlightNotificationHelper.show(flight)
    runtime.container.liveUpdateRefreshScheduler.schedule(flight)
    runtime.state.restore(flight, null)
}

private suspend fun restoreLandedItineraryLeg(flight: ActiveFlight, runtime: TravelBlockRuntime) {
    val points = FlightRewardCalculator.completedReward(flight)
    recordRestoredLeg(runtime.container, flight, points)
    val minutes = flight.breakAfterLeg(flight.itineraryLegIndex)
    val next = flight.copy(
        status = com.travelblock.app.domain.model.ActiveFlightStatus.Layover,
        itineraryPointsEarned = flight.itineraryPointsEarned + points,
        layoverEndsAtEpochMillis = flight.plannedArrivalAt.toEpochMilli() +
            java.time.Duration.ofMinutes(minutes.toLong()).toMillis(),
    )
    runtime.container.activeFlightSessionRepository.saveActiveFlight(next)
    restoreLayover(next, runtime)
}

private suspend fun restoreArrivedFlight(flight: ActiveFlight, runtime: TravelBlockRuntime) {
    recordRestoredArrival(runtime.container, flight)
    runtime.container.activeFlightSessionRepository.clearActiveFlight()
    runtime.container.landingAlarmScheduler.cancel()
    runtime.container.liveUpdateRefreshScheduler.cancel()
    runtime.container.liveFlightNotificationHelper.cancel()
    runtime.container.notificationHelper.cancelFlightLandedNotification()
    runtime.state.restore(
        null,
        ArrivalSummary.fromCompletedFlight(
            flight = flight,
            pointsEarned = FlightRewardCalculator.completedReward(flight) +
                if (flight.isItinerary) com.travelblock.app.domain.engine.ItineraryRewards.completionBonus(flight.itineraryFocusMinutes) else 0,
            routeAirports = runtime.container.airportRepository.getAllAirports(),
        ),
    )
}

@Composable
internal fun OpenRestoredArrivalEffect(
    inputs: ArrivalEffectInputs,
    onOpenArrival: () -> Unit,
) {
    LaunchedEffect(inputs.activeFlightRestored, inputs.arrivalSummary, inputs.openArrivalRequested) {
        if (shouldOpenRestoredArrival(inputs)) onOpenArrival()
    }
}

private fun shouldOpenRestoredArrival(inputs: ArrivalEffectInputs): Boolean {
    if (!inputs.activeFlightRestored || inputs.arrivalSummary == null) return false
    return inputs.openArrivalRequested || inputs.currentRoute == null ||
        inputs.currentRoute == TravelBlockDestination.Home.route
}

@Composable
internal fun LiveFlightUpdatesEffect(
    flight: ActiveFlight?,
    container: TravelBlockContainer,
) {
    LaunchedEffect(flight?.startedAt, flight?.status, flight?.itineraryLegIndex, flight?.layoverEndsAtEpochMillis) {
        if (flight == null) return@LaunchedEffect
        if (!container.liveFlightNotificationHelper.canPostNotifications()) return@LaunchedEffect
        container.liveFlightNotificationHelper.show(flight)
        container.liveUpdateRefreshScheduler.schedule(flight)
        while (true) {
            delay(60_000L)
            container.liveFlightNotificationHelper.show(flight)
        }
    }
}
