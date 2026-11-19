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
import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.ArrivalSummary
import com.travelblock.app.domain.model.BoardingPass
import com.travelblock.app.domain.model.DiversionSummary
import com.travelblock.app.domain.model.DraftFlightBooking
import com.travelblock.app.ui.navigation.TravelBlockBottomBar
import com.travelblock.app.ui.navigation.TravelBlockDestination
import com.travelblock.app.ui.screen.arrival.ArrivalScreen
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
import com.travelblock.app.ui.util.AudioPlaybackSettings
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.travelblock.app.MainActivity
import com.travelblock.app.ui.theme.TravelBlockTheme

@Composable
fun TravelBlockApp(openArrivalRequested: Boolean = false) {
    val context = LocalContext.current.applicationContext
    val container = remember(context) { TravelBlockContainer(context) }
    val settings by container.settingsRepository.settings.collectAsState(initial = UserSettings())
    val darkTheme = when (settings.selectedTheme) {
        "dark" -> true
        "light" -> false
        else -> null
    }
    TravelBlockTheme(darkTheme = darkTheme) {
        TravelBlockAppContent(container, settings, openArrivalRequested)
    }
}

@Composable
private fun TravelBlockAppContent(
    container: TravelBlockContainer,
    settings: UserSettings,
    openArrivalRequested: Boolean,
) {
    val activity = LocalContext.current as? MainActivity
    val context = LocalContext.current.applicationContext
    val state = remember { TravelBlockAppState() }
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val pointsBalance by container.pointsRepository.observePointsBalance().collectAsState(initial = 0)
    val coroutineScope = rememberCoroutineScope()
    val audioCuePlayer = remember(context) { FlightAudioCuePlayer(context) }
    val runtime = TravelBlockRuntime(container, state, settings, pointsBalance, coroutineScope, audioCuePlayer)

    TravelBlockEffects(runtime, navController, currentRoute, openArrivalRequested)
    if (settings.onboardingComplete) {
        TravelBlockShell(navController, currentRoute, runtime)
    } else {
        OnboardingFlow(container, coroutineScope, activity)
    }
}

@Composable
private fun OnboardingFlow(
    container: TravelBlockContainer,
    coroutineScope: CoroutineScope,
    activity: MainActivity?,
) {
    OnboardingScreen(
        airports = remember(container) { container.airportRepository.getAllAirports() },
        onFinish = { homeAirport, requestNotifications ->
            coroutineScope.launch {
                homeAirport?.let {
                    container.settingsRepository.setHomeAirportCode(AirportCode(it.code))
                    container.settingsRepository.setCurrentAirportCode(AirportCode(it.code))
                }
                if (requestNotifications) activity?.requestNotificationPermissionIfNeeded()
                container.settingsRepository.setOnboardingComplete(SettingEnabled(true))
            }
        },
    )
}

private class TravelBlockAppState {
    var draftFlightBooking by mutableStateOf<DraftFlightBooking?>(null)
    var quickBoardingPass by mutableStateOf<BoardingPass?>(null)
    var activeFlight by mutableStateOf<ActiveFlight?>(null)
    var arrivalSummary by mutableStateOf<ArrivalSummary?>(null)
    var diversionSummary by mutableStateOf<DiversionSummary?>(null)
    var activeFlightRestored by mutableStateOf(false)
    var restoreRouteHandled by mutableStateOf(false)

    fun restore(flight: ActiveFlight?, arrival: ArrivalSummary?) {
        activeFlight = flight
        arrivalSummary = arrival
        activeFlightRestored = true
    }
}

private data class TravelBlockRuntime(
    val container: TravelBlockContainer,
    val state: TravelBlockAppState,
    val settings: UserSettings,
    val pointsBalance: Int,
    val coroutineScope: CoroutineScope,
    val audioCuePlayer: FlightAudioCuePlayer,
)

@Composable
private fun TravelBlockEffects(
    runtime: TravelBlockRuntime,
    navController: NavHostController,
    currentRoute: String?,
    openArrivalRequested: Boolean,
) {
    DisposableEffect(runtime.audioCuePlayer) {
        onDispose { runtime.audioCuePlayer.release() }
    }
    RestoreActiveFlightEffect(runtime)
    OpenRestoredArrivalEffect(
        ArrivalEffectInputs(
            activeFlightRestored = runtime.state.activeFlightRestored,
            arrivalSummary = runtime.state.arrivalSummary,
            openArrivalRequested = openArrivalRequested,
            currentRoute = currentRoute,
        ),
    ) {
        navigateToArrival(navController)
    }
    RestoreCockpitRouteEffect(runtime, navController, currentRoute)
}

private data class ArrivalEffectInputs(
    val activeFlightRestored: Boolean,
    val arrivalSummary: ArrivalSummary?,
    val openArrivalRequested: Boolean,
    val currentRoute: String?,
)

@Composable
private fun RestoreCockpitRouteEffect(
    runtime: TravelBlockRuntime,
    navController: NavHostController,
    currentRoute: String?,
) {
    val state = runtime.state
    LaunchedEffect(state.activeFlightRestored, state.activeFlight, currentRoute, state.restoreRouteHandled) {
        val shouldRoute = shouldAutoRouteToCockpit(
            activeFlightRestored = state.activeFlightRestored,
            hasActiveFlight = state.activeFlight != null,
            currentRoute = currentRoute,
            restoreRouteHandled = state.restoreRouteHandled,
        )
        if (!shouldRoute) return@LaunchedEffect
        state.restoreRouteHandled = true
        navigateToCockpit(navController, popHome = true)
    }
}

@Composable
private fun TravelBlockShell(
    navController: NavHostController,
    currentRoute: String?,
    runtime: TravelBlockRuntime,
) {
    Scaffold(
        contentWindowInsets = if (isImmersiveRoute(currentRoute)) WindowInsets(0, 0, 0, 0) else ScaffoldDefaults.contentWindowInsets,
        bottomBar = { TravelBlockBottomBarHost(navController, currentRoute, runtime.state.activeFlight != null) },
    ) { innerPadding ->
        TravelBlockNavGraph(navController, runtime, Modifier.padding(innerPadding))
    }
}

@Composable
private fun TravelBlockBottomBarHost(
    navController: NavHostController,
    currentRoute: String?,
    activeFlightInProgress: Boolean,
) {
    if (shouldShowBottomBar(currentRoute)) {
        TravelBlockBottomBar(
            currentRoute = currentRoute,
            destinations = bottomTabs(activeFlightInProgress),
            onDestinationSelected = { destination ->
                navController.navigate(destination.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
        )
    }
}

/** Screens whose scenery runs behind the status bar and which pad their own content for system bars. */
private fun isImmersiveRoute(currentRoute: String?): Boolean =
    currentRoute in setOf(
        TravelBlockDestination.Home.route,
        TravelBlockDestination.Cockpit.route,
        TravelBlockDestination.Arrival.route,
    )

private fun shouldShowBottomBar(currentRoute: String?): Boolean =
    currentRoute !in setOf(
        TravelBlockDestination.Book.route,
        TravelBlockDestination.QuickBoarding.route,
        TravelBlockDestination.Cockpit.route,
        TravelBlockDestination.Arrival.route,
        TravelBlockDestination.Diversion.route,
    )

private fun bottomTabs(activeFlightInProgress: Boolean) =
    if (activeFlightInProgress) TravelBlockDestination.activeFlightBottomTabs
    else TravelBlockDestination.normalBottomTabs

@Composable
private fun TravelBlockNavGraph(
    navController: NavHostController,
    runtime: TravelBlockRuntime,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = TravelBlockDestination.Home.route,
        modifier = modifier,
    ) {
        homeRoute(navController, runtime)
        bookingRoute(navController, runtime)
        quickBoardingRoute(navController, runtime)
        cockpitRoute(navController, runtime)
        arrivalRoute(navController, runtime)
        diversionRoute(navController, runtime)
        logbookRoute(navController, runtime)
        logbookDetailRoute(navController, runtime)
        composable(TravelBlockDestination.Store.route) { StoreScreen(runtime.container) }
        composable(TravelBlockDestination.Settings.route) { SettingsScreen(runtime.container) }
    }
}

private fun NavGraphBuilder.homeRoute(
    navController: NavHostController,
    runtime: TravelBlockRuntime,
) {
    composable(TravelBlockDestination.Home.route) {
        HomeScreen(
            container = runtime.container,
            activeFlight = runtime.state.activeFlight,
            onReturnToCockpit = { navigateToCockpit(navController) },
            onBookSelectedRoute = { draft ->
                if (runtime.state.activeFlight == null) {
                    runtime.state.draftFlightBooking = draft
                    navigateToBooking(navController)
                }
            },
            onStartQuickFlight = { draft ->
                if (runtime.state.activeFlight == null) {
                    runtime.state.quickBoardingPass = createQuickBoardingPass(draft)
                    navController.navigate(TravelBlockDestination.QuickBoarding.route)
                }
            },
        )
    }
}

private fun NavGraphBuilder.quickBoardingRoute(
    navController: NavHostController,
    runtime: TravelBlockRuntime,
) {
    composable(TravelBlockDestination.QuickBoarding.route) {
        runtime.state.quickBoardingPass?.let { pass ->
            QuickBoardingScreen(
                boardingPass = pass,
                onBack = { navController.popBackStack() },
                onStartFlight = { startQuickFlight(pass, runtime, navController) },
            )
        }
    }
}

private fun startQuickFlight(pass: BoardingPass, runtime: TravelBlockRuntime, navController: NavHostController) {
    playCue(runtime, FlightAudioCue.FlightStarted)
    val flight = pass.toActiveFlight(startedAt = Instant.now()).withDebugFlightDuration(runtime.settings)
    runtime.state.activeFlight = flight
    runtime.coroutineScope.launch {
        runtime.container.activeFlightSessionRepository.saveActiveFlight(flight)
        runtime.container.landingAlarmScheduler.schedule(flight)
        navigateToCockpit(navController, popHome = true)
    }
}

private fun NavGraphBuilder.bookingRoute(
    navController: NavHostController,
    runtime: TravelBlockRuntime,
) {
    composable(TravelBlockDestination.Book.route) {
        BookScreen(
            draftFlightBooking = runtime.state.draftFlightBooking,
            pointsBalance = runtime.pointsBalance,
            hapticsEnabled = runtime.settings.hapticsEnabled,
            soundEnabled = runtime.settings.soundEnabled,
            boardingPassThemeId = runtime.settings.equippedBoardingPassThemeId,
            onGateCleared = { playCue(runtime, FlightAudioCue.GateCleared) },
            onCancelBooking = { navController.popBackStack(TravelBlockDestination.Home.route, false) },
            onStartFocusFlight = { pass -> startFocusFlight(pass, runtime, navController) },
        )
    }
}

private fun startFocusFlight(
    pass: com.travelblock.app.domain.model.BoardingPass,
    runtime: TravelBlockRuntime,
    navController: NavHostController,
) {
    playCue(runtime, FlightAudioCue.FlightStarted)
    val activeFlight = pass.toActiveFlight(startedAt = Instant.now()).withDebugFlightDuration(runtime.settings)
    runtime.state.activeFlight = activeFlight
    runtime.coroutineScope.launch {
        runtime.container.activeFlightSessionRepository.saveActiveFlight(activeFlight)
        runtime.container.landingAlarmScheduler.schedule(activeFlight)
        navigateToCockpit(navController, popHome = true)
        val transaction = createSeatUpgradeTransaction(activeFlight, Instant.now())
        if (transaction != null) runtime.container.pointsRepository.insertTransaction(transaction)
    }
}

private fun NavGraphBuilder.cockpitRoute(
    navController: NavHostController,
    runtime: TravelBlockRuntime,
) {
    composable(TravelBlockDestination.Cockpit.route) {
        CockpitScreen(
            container = runtime.container,
            activeFlight = runtime.state.activeFlight,
            actions = CockpitActions(
                onFlightArrived = { summary -> completeFlight(summary, runtime, navController) },
                onFlightDiverted = { summary -> divertFlight(summary, runtime, navController) },
                onPrepareForLanding = {
                    runtime.audioCuePlayer.announce(
                        FlightAudioCue.PrepareForLanding,
                        settings = AudioPlaybackSettings(
                            soundEnabled = runtime.settings.soundEnabled,
                            announcementsEnabled = runtime.settings.announcementsEnabled,
                        ),
                    )
                },
            ),
        )
    }
}

private fun completeFlight(
    summary: ArrivalSummary,
    runtime: TravelBlockRuntime,
    navController: NavHostController,
) {
    playCue(runtime, FlightAudioCue.FlightArrived)
    runtime.container.notificationHelper.showFlightLandedNotification(
        flightNumber = summary.flightNumber,
        destinationCode = summary.destination.displayCode,
    )
    runtime.container.landingAlarmScheduler.cancel()
    runtime.state.arrivalSummary = summary
    runtime.state.activeFlight = null
    runtime.coroutineScope.launch { runtime.container.activeFlightSessionRepository.clearActiveFlight() }
    navigateToArrival(navController, popCockpit = true)
}

private fun divertFlight(
    summary: DiversionSummary,
    runtime: TravelBlockRuntime,
    navController: NavHostController,
) {
    playCue(runtime, FlightAudioCue.FlightDiverted)
    runtime.container.landingAlarmScheduler.cancel()
    runtime.state.diversionSummary = summary
    runtime.state.activeFlight = null
    runtime.coroutineScope.launch { runtime.container.activeFlightSessionRepository.clearActiveFlight() }
    navigateToDiversion(navController)
}

private fun NavGraphBuilder.arrivalRoute(
    navController: NavHostController,
    runtime: TravelBlockRuntime,
) {
    composable(TravelBlockDestination.Arrival.route) {
        ArrivalScreen(
            arrivalSummary = runtime.state.arrivalSummary,
            homeAirportCode = runtime.settings.homeAirportCode,
            onContinueJourney = { navigateToHome(navController) },
            onViewLogbook = { navigateToLogbook(navController) },
        )
    }
}

private fun NavGraphBuilder.diversionRoute(
    navController: NavHostController,
    runtime: TravelBlockRuntime,
) {
    composable(TravelBlockDestination.Diversion.route) {
        DiversionScreen(
            diversionSummary = runtime.state.diversionSummary,
            onReturnHome = { navigateToHome(navController) },
            onTryAgain = { navigateToHome(navController) },
            onViewLogbook = { navigateToLogbook(navController) },
        )
    }
}

private fun NavGraphBuilder.logbookRoute(
    navController: NavHostController,
    runtime: TravelBlockRuntime,
) {
    composable(TravelBlockDestination.Logbook.route) {
        LogbookScreen(
            flightLogRepository = runtime.container.flightLogRepository,
            onFlightSelected = { id ->
                navController.navigate(TravelBlockDestination.LogbookDetail.createRoute(id))
            },
        )
    }
}

private fun NavGraphBuilder.logbookDetailRoute(
    navController: NavHostController,
    runtime: TravelBlockRuntime,
) {
    composable(
        route = TravelBlockDestination.LogbookDetail.route,
        arguments = listOf(
            navArgument(TravelBlockDestination.LogbookDetail.FlightIdArg) {
                type = NavType.StringType
            },
        ),
    ) { entry ->
        val id = entry.arguments?.getString(TravelBlockDestination.LogbookDetail.FlightIdArg).orEmpty()
        FlightLogDetailScreen(
            flightId = id,
            flightLogRepository = runtime.container.flightLogRepository,
            onBack = { navController.popBackStack() },
        )
    }
}

private fun playCue(runtime: TravelBlockRuntime, cue: FlightAudioCue) {
    runtime.audioCuePlayer.play(
        cue,
        settings = AudioPlaybackSettings(
            soundEnabled = runtime.settings.soundEnabled,
            announcementsEnabled = runtime.settings.announcementsEnabled,
        ),
    )
}

private fun navigateToBooking(navController: NavHostController) {
    navController.navigate(TravelBlockDestination.Book.route) {
        popUpTo(navController.graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun navigateToCockpit(navController: NavHostController, popHome: Boolean = false) {
    navController.navigate(TravelBlockDestination.Cockpit.route) {
        if (popHome) popUpTo(TravelBlockDestination.Home.route) { inclusive = false }
        launchSingleTop = true
    }
}

private fun navigateToArrival(navController: NavHostController, popCockpit: Boolean = false) {
    navController.navigate(TravelBlockDestination.Arrival.route) {
        if (popCockpit) popUpTo(TravelBlockDestination.Cockpit.route) { inclusive = true }
        else popUpTo(TravelBlockDestination.Home.route) { inclusive = false }
        launchSingleTop = true
    }
}

private fun navigateToDiversion(navController: NavHostController) {
    navController.navigate(TravelBlockDestination.Diversion.route) {
        popUpTo(TravelBlockDestination.Cockpit.route) { inclusive = true }
        launchSingleTop = true
    }
}

private fun navigateToHome(navController: NavHostController) {
    navController.navigate(TravelBlockDestination.Home.route) {
        popUpTo(navController.graph.startDestinationId) { inclusive = false }
        launchSingleTop = true
    }
}

private fun navigateToLogbook(navController: NavHostController) {
    navController.navigate(TravelBlockDestination.Logbook.route) {
        popUpTo(navController.graph.startDestinationId) { inclusive = false }
        launchSingleTop = true
    }
}

internal fun createSeatUpgradeTransaction(
    activeFlight: ActiveFlight,
    now: Instant,
): PointsTransactionEntity? {
    val seatCost = activeFlight.seat.pointsCost
    if (seatCost <= 0) return null
    return PointsTransactionEntity(
        id = UUID.randomUUID().toString(),
        amount = -seatCost,
        reason = "Seat upgrade ${activeFlight.seat.label}",
        createdAt = now.toEpochMilli(),
        relatedFlightId = null,
    )
}

internal fun shouldAutoRouteToCockpit(
    activeFlightRestored: Boolean,
    hasActiveFlight: Boolean,
    currentRoute: String?,
    restoreRouteHandled: Boolean,
): Boolean {
    if (!activeFlightRestored || !hasActiveFlight || restoreRouteHandled) return false
    return currentRoute == null || currentRoute == TravelBlockDestination.Home.route
}

internal fun ActiveFlight.withDebugFlightDuration(settings: UserSettings): ActiveFlight {
    if (!settings.debugShortFlightsEnabled || !com.travelblock.app.BuildConfig.DEBUG) return this
    return copy(plannedArrivalAt = startedAt.plusSeconds(settings.debugFlightDurationSeconds.toLong()))
}

internal fun shouldRestoreAsArrival(plannedArrivalAt: Instant, now: Instant): Boolean =
    !plannedArrivalAt.isAfter(now)

private suspend fun recordRestoredArrival(
    container: TravelBlockContainer,
    flight: ActiveFlight,
) {
    val flightId = UUID.nameUUIDFromBytes(
        "${flight.flightNumber}:${flight.startedAt.toEpochMilli()}".toByteArray(),
    ).toString()
    val endedAt = flight.plannedArrivalAt.toEpochMilli()
    val pointsEarned = FlightRewardCalculator.completedReward(flight)
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
                    container.settingsRepository.setCurrentAirportCode(AirportCode(flight.destination.code))
    container.activeFlightSessionRepository.clearActiveFlight()
}

@Composable
private fun RestoreActiveFlightEffect(runtime: TravelBlockRuntime) {
    val state = runtime.state
    LaunchedEffect(state.activeFlightRestored) {
        if (state.activeFlightRestored) return@LaunchedEffect
        val flight = runtime.container.activeFlightSessionRepository.getActiveFlight()
        val hasArrived = flight != null && shouldRestoreAsArrival(flight.plannedArrivalAt, Instant.now())
        if (hasArrived) {
            restoreArrivedFlight(requireNotNull(flight), runtime)
        } else {
            flight?.let(runtime.container.landingAlarmScheduler::schedule)
            state.restore(flight, null)
        }
    }
}

private suspend fun restoreArrivedFlight(flight: ActiveFlight, runtime: TravelBlockRuntime) {
    recordRestoredArrival(runtime.container, flight)
    runtime.container.landingAlarmScheduler.cancel()
    runtime.container.notificationHelper.cancelFlightLandedNotification()
    runtime.state.restore(
        null,
        ArrivalSummary.fromCompletedFlight(
            flight = flight,
            pointsEarned = FlightRewardCalculator.completedReward(flight),
        ),
    )
}

@Composable
private fun OpenRestoredArrivalEffect(
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
