package com.travelblock.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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
import com.travelblock.app.TravelBlockContainer
import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.ArrivalSummary
import com.travelblock.app.domain.model.DiversionSummary
import com.travelblock.app.domain.model.DraftFlightBooking
import com.travelblock.app.data.database.PointsTransactionEntity
import com.travelblock.app.data.settings.UserSettings
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.travelblock.app.ui.navigation.TravelBlockBottomBar
import com.travelblock.app.ui.navigation.TravelBlockDestination
import com.travelblock.app.ui.screen.arrival.ArrivalScreen
import com.travelblock.app.ui.screen.book.BookScreen
import com.travelblock.app.ui.screen.cockpit.CockpitScreen
import com.travelblock.app.ui.screen.diversion.DiversionScreen
import com.travelblock.app.ui.screen.home.HomeScreen
import com.travelblock.app.ui.screen.logbook.FlightLogDetailScreen
import com.travelblock.app.ui.screen.logbook.LogbookScreen
import com.travelblock.app.ui.screen.settings.SettingsScreen
import com.travelblock.app.ui.screen.store.StoreScreen
import com.travelblock.app.ui.util.FlightAudioCue
import com.travelblock.app.ui.util.FlightAudioCuePlayer
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.launch

@Composable
fun TravelBlockApp() {
    val context = LocalContext.current.applicationContext
    val container = remember(context) { TravelBlockContainer(context) }
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val settings by container.settingsRepository.settings.collectAsState(initial = UserSettings())
    val pointsBalance by container.pointsRepository.observePointsBalance().collectAsState(initial = 0)
    val coroutineScope = rememberCoroutineScope()
    val audioCuePlayer = remember { FlightAudioCuePlayer() }
    var draftFlightBooking by remember { mutableStateOf<DraftFlightBooking?>(null) }
    var activeFlight by remember { mutableStateOf<ActiveFlight?>(null) }
    var arrivalSummary by remember { mutableStateOf<ArrivalSummary?>(null) }
    var diversionSummary by remember { mutableStateOf<DiversionSummary?>(null) }
    var activeFlightRestored by remember { mutableStateOf(false) }
    var restoreRouteHandled by remember { mutableStateOf(false) }
    val showBottomBar = currentRoute !in setOf(
        TravelBlockDestination.Book.route,
        TravelBlockDestination.Cockpit.route,
        TravelBlockDestination.Arrival.route,
        TravelBlockDestination.Diversion.route,
    )
    val activeFlightInProgress = activeFlight != null

    LaunchedEffect(activeFlightRestored) {
        if (activeFlightRestored) return@LaunchedEffect
        activeFlight = container.activeFlightSessionRepository.getActiveFlight()
        activeFlightRestored = true
    }

    DisposableEffect(Unit) {
        onDispose {
            audioCuePlayer.release()
        }
    }

    LaunchedEffect(activeFlightRestored, activeFlight, currentRoute, restoreRouteHandled) {
        if (!shouldAutoRouteToCockpit(
                activeFlightRestored = activeFlightRestored,
                hasActiveFlight = activeFlight != null,
                currentRoute = currentRoute,
                restoreRouteHandled = restoreRouteHandled,
            )
        ) {
            return@LaunchedEffect
        }
        restoreRouteHandled = true
        navController.navigate(TravelBlockDestination.Cockpit.route) {
            popUpTo(TravelBlockDestination.Home.route) {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                TravelBlockBottomBar(
                    currentRoute = currentRoute,
                    destinations = if (activeFlightInProgress) {
                        TravelBlockDestination.activeFlightBottomTabs
                    } else {
                        TravelBlockDestination.normalBottomTabs
                    },
                    onDestinationSelected = { destination ->
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TravelBlockDestination.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(TravelBlockDestination.Home.route) {
                HomeScreen(
                    container = container,
                    activeFlight = activeFlight,
                    onReturnToCockpit = {
                        navController.navigate(TravelBlockDestination.Cockpit.route) {
                            launchSingleTop = true
                        }
                    },
                    onBookSelectedRoute = { draft ->
                        if (!activeFlightInProgress) {
                            draftFlightBooking = draft
                            navController.navigate(TravelBlockDestination.Book.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                )
            }
            composable(TravelBlockDestination.Book.route) {
                BookScreen(
                    draftFlightBooking = draftFlightBooking,
                    pointsBalance = pointsBalance,
                    hapticsEnabled = settings.hapticsEnabled,
                    soundEnabled = settings.soundEnabled,
                    boardingPassThemeId = settings.equippedBoardingPassThemeId,
                    onGateCleared = {
                        audioCuePlayer.play(FlightAudioCue.GateCleared, enabled = settings.soundEnabled)
                    },
                    onCancelBooking = {
                        navController.popBackStack(
                            route = TravelBlockDestination.Home.route,
                            inclusive = false,
                        )
                    },
                    onStartFocusFlight = { boardingPass ->
                        audioCuePlayer.play(FlightAudioCue.FlightStarted, enabled = settings.soundEnabled)
                        val nextActiveFlight = boardingPass.toActiveFlight(startedAt = Instant.now())
                        activeFlight = nextActiveFlight
                        coroutineScope.launch {
                            container.activeFlightSessionRepository.saveActiveFlight(nextActiveFlight)
                            val seatUpgradeTransaction = createSeatUpgradeTransaction(nextActiveFlight, Instant.now())
                            if (seatUpgradeTransaction != null) {
                                container.pointsRepository.insertTransaction(seatUpgradeTransaction)
                            }
                        }
                        navController.navigate(TravelBlockDestination.Cockpit.route) {
                            popUpTo(TravelBlockDestination.Home.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(TravelBlockDestination.Cockpit.route) {
                CockpitScreen(
                    container = container,
                    activeFlight = activeFlight,
                    onFlightArrived = { summary ->
                        audioCuePlayer.play(FlightAudioCue.FlightArrived, enabled = settings.soundEnabled)
                        container.notificationHelper.showFlightLandedNotification(
                            flightNumber = summary.flightNumber,
                            destinationCode = summary.destination.displayCode,
                        )
                        arrivalSummary = summary
                        activeFlight = null
                        coroutineScope.launch {
                            container.activeFlightSessionRepository.clearActiveFlight()
                        }
                        navController.navigate(TravelBlockDestination.Arrival.route) {
                            popUpTo(TravelBlockDestination.Cockpit.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onFlightDiverted = { summary ->
                        audioCuePlayer.play(FlightAudioCue.FlightDiverted, enabled = settings.soundEnabled)
                        diversionSummary = summary
                        activeFlight = null
                        coroutineScope.launch {
                            container.activeFlightSessionRepository.clearActiveFlight()
                        }
                        navController.navigate(TravelBlockDestination.Diversion.route) {
                            popUpTo(TravelBlockDestination.Cockpit.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(TravelBlockDestination.Arrival.route) {
                ArrivalScreen(
                    arrivalSummary = arrivalSummary,
                    homeAirportCode = settings.homeAirportCode,
                    onContinueJourney = {
                        navController.navigate(TravelBlockDestination.Home.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                    onViewLogbook = {
                        navController.navigate(TravelBlockDestination.Logbook.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(TravelBlockDestination.Diversion.route) {
                DiversionScreen(
                    diversionSummary = diversionSummary,
                    onReturnHome = {
                        navController.navigate(TravelBlockDestination.Home.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                    onTryAgain = {
                        navController.navigate(TravelBlockDestination.Home.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                    onViewLogbook = {
                        navController.navigate(TravelBlockDestination.Logbook.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(TravelBlockDestination.Logbook.route) {
                LogbookScreen(
                    flightLogRepository = container.flightLogRepository,
                    onFlightSelected = { flightId ->
                        navController.navigate(TravelBlockDestination.LogbookDetail.createRoute(flightId))
                    },
                )
            }
            composable(
                route = TravelBlockDestination.LogbookDetail.route,
                arguments = listOf(
                    navArgument(TravelBlockDestination.LogbookDetail.FlightIdArg) {
                        type = NavType.StringType
                    },
                ),
            ) { backStackEntry ->
                val flightId = backStackEntry.arguments
                    ?.getString(TravelBlockDestination.LogbookDetail.FlightIdArg)
                    .orEmpty()
                FlightLogDetailScreen(
                    flightId = flightId,
                    flightLogRepository = container.flightLogRepository,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(TravelBlockDestination.Store.route) { StoreScreen(container) }
            composable(TravelBlockDestination.Settings.route) { SettingsScreen(container) }
        }
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
