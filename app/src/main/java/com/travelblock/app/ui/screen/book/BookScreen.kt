package com.travelblock.app.ui.screen.book

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.BoardingPass
import com.travelblock.app.domain.model.DraftFlightBooking
import com.travelblock.app.domain.model.GeoPoint
import com.travelblock.app.domain.model.SeatOption
import com.travelblock.app.domain.engine.HubConnection
import com.travelblock.app.domain.model.TicketClass
import com.travelblock.app.domain.model.SessionPlan
import com.travelblock.app.data.todoist.TodoistRepository
import com.travelblock.app.ui.components.BoardingPassCard
import com.travelblock.app.ui.components.BoardingPassReveal
import com.travelblock.app.ui.components.BookingFlowStep
import com.travelblock.app.ui.components.BookingFlowTransition
import com.travelblock.app.ui.components.BookingProgressRail
import com.travelblock.app.ui.components.BookingRitualSurface
import com.travelblock.app.ui.components.BookingRitualSurfaceTransition
import com.travelblock.app.ui.components.CompactRouteDocket
import com.travelblock.app.ui.components.DocketFlightDetails
import com.travelblock.app.ui.components.DocketRoute
import com.travelblock.app.ui.components.EmptyState
import com.travelblock.app.ui.components.EmptyStateContent
import com.travelblock.app.ui.components.GateBoardingSequence
import com.travelblock.app.ui.components.GateBoardingInfo
import com.travelblock.app.ui.components.Kicker
import com.travelblock.app.ui.components.MapStop
import com.travelblock.app.ui.components.PaperCard
import com.travelblock.app.ui.components.PrimaryButton
import com.travelblock.app.ui.components.SecondaryButton
import com.travelblock.app.ui.components.SoftChip
import com.travelblock.app.ui.components.StampPill
import com.travelblock.app.ui.components.StickyActionScaffold
import com.travelblock.app.ui.components.StickyActionListOptions
import com.travelblock.app.ui.components.TravelDayBackdrop
import com.travelblock.app.ui.components.TravelMap
import com.travelblock.app.ui.scene.CabinAisleScene
import com.travelblock.app.ui.scene.CheckInDeskScene
import com.travelblock.app.ui.scene.CheckInGateInfo
import com.travelblock.app.ui.scene.JetBridgeScene
import com.travelblock.app.ui.theme.Cream
import com.travelblock.app.ui.theme.DuskBlue
import com.travelblock.app.ui.theme.Ink
import com.travelblock.app.ui.theme.Marigold
import com.travelblock.app.ui.theme.Paper
import com.travelblock.app.ui.theme.Sage
import com.travelblock.app.ui.theme.Sand
import com.travelblock.app.ui.theme.SkyMist
import com.travelblock.app.ui.theme.SoftSky
import com.travelblock.app.ui.theme.WarmGray
import com.travelblock.app.ui.util.rememberTravelBlockHaptics

@Composable
fun BookScreen(
    draftFlightBooking: DraftFlightBooking?,
    pointsBalance: Int,
    hapticsEnabled: Boolean = true,
    soundEnabled: Boolean = true,
    boardingPassThemeId: String = "",
    onGateCleared: () -> Unit = {},
    onCancelBooking: () -> Unit,
    onStartFocusFlight: (BoardingPass) -> Unit,
    todoistRepository: TodoistRepository? = null,
    onOpenTodoistSettings: () -> Unit = {},
    airports: List<Airport> = emptyList(),
) {
    val viewModel: BookingViewModel = viewModel(
        key = draftFlightBooking?.let {
            "${it.origin.code}-${it.destination.code}-${it.durationMinutes}-$pointsBalance"
        } ?: "empty-booking",
        factory = BookingViewModelFactory(draftFlightBooking, pointsBalance),
    )

    BookScreenContent(
        uiState = viewModel.uiState,
        hapticsEnabled = hapticsEnabled,
        soundEnabled = soundEnabled,
        onCancelBooking = onCancelBooking,
        onSeatSelected = viewModel::selectSeat,
        onObjectiveChanged = viewModel::updateFocusObjective,
        onFocusTagSelected = viewModel::selectFocusTag,
        onTripPresetSelected = viewModel::selectTripPreset,
        onSessionPlanSelected = viewModel::selectSessionPlan,
        onTripDestinationSelected = viewModel::selectTripDestination,
        onHubSelected = viewModel::selectHubConnection,
        onTodoistTaskSelected = viewModel::toggleTodoistTask,
        overview = TripOverviewActions(viewModel::setTaskPerLeg, viewModel::continueFromOverview, onCancelBooking),
        onPreflightCheckChanged = viewModel::setPreflightCheck,
        onContinueToSeatSelection = viewModel::continueToSeatSelection,
        // Check-in only happens once per trip: from a later leg's seat map, back means back to the layover.
        onBackToManifest = if (draftFlightBooking?.continuation?.itineraryTaskPerLeg == false) onCancelBooking else viewModel::returnToManifest,
        onContinue = viewModel::continueToBoardingPass,
        onBoardFlight = viewModel::boardFlight,
        onBackToReview = viewModel::returnToReview,
        boardingPassThemeId = boardingPassThemeId,
        onGateCleared = onGateCleared,
        onStartFocusFlight = onStartFocusFlight,
        todoistRepository = todoistRepository,
        onOpenTodoistSettings = onOpenTodoistSettings,
        airports = airports,
    )
}

@Composable
fun BookScreenContent(
    uiState: BookingUiState,
    hapticsEnabled: Boolean = true,
    soundEnabled: Boolean = true,
    onCancelBooking: () -> Unit,
    onSeatSelected: (String) -> Unit,
    onObjectiveChanged: (String) -> Unit,
    onFocusTagSelected: (String) -> Unit,
    onPreflightCheckChanged: (String, Boolean) -> Unit,
    onContinueToSeatSelection: () -> Unit,
    onBackToManifest: () -> Unit,
    onContinue: () -> Unit,
    onBoardFlight: () -> Unit,
    onBackToReview: () -> Unit,
    boardingPassThemeId: String = "",
    onGateCleared: () -> Unit = {},
    onStartFocusFlight: (BoardingPass) -> Unit,
    onTodoistTaskSelected: (com.travelblock.app.data.todoist.TodoistTask?) -> Unit = {},
    todoistRepository: TodoistRepository? = null,
    onOpenTodoistSettings: () -> Unit = {},
    onTripPresetSelected: (com.travelblock.app.domain.model.TripPreset?) -> Unit = {},
    onSessionPlanSelected: (SessionPlan?) -> Unit = {},
    onTripDestinationSelected: (Airport) -> Unit = {},
    onHubSelected: (HubConnection?) -> Unit = {},
    airports: List<Airport> = emptyList(),
    overview: TripOverviewActions = TripOverviewActions(),
) {
    if (!uiState.draftAvailable) {
        EmptyBookingState()
        return
    }
    if (uiState.showTripOverview) {
        TripOverviewScreen(uiState, overview)
        return
    }

    val haptics = rememberTravelBlockHaptics(enabled = hapticsEnabled)
    var ritualForward by remember { mutableStateOf(true) }
    val boardingPass = uiState.boardingPass

    val ritualSurface = when {
        uiState.showGateCall -> BookingRitualSurface.GateCall
        uiState.showBoardingPass -> BookingRitualSurface.BoardingPass
        else -> BookingRitualSurface.CheckIn
    }

    LaunchedEffect(uiState.showBoardingPass) {
        if (uiState.showBoardingPass) {
            haptics.boardingPassIssued()
        }
    }

    TravelDayBackdrop(modifier = Modifier.fillMaxSize()) {
        BookingRitualSurfaceTransition(
            surface = ritualSurface,
            forward = ritualForward,
            modifier = Modifier.fillMaxSize(),
        ) { surface ->
            when (surface) {
                BookingRitualSurface.CheckIn -> CheckInSurface(
                    uiState = uiState,
                    ritualForward = ritualForward,
                    onCancelBooking = onCancelBooking,
                    onSeatSelected = { seatId ->
                        onSeatSelected(seatId)
                        haptics.seatSelected()
                    },
                    onObjectiveChanged = onObjectiveChanged,
                    onFocusTagSelected = onFocusTagSelected,
                    onTripPresetSelected = onTripPresetSelected,
                    onSessionPlanSelected = onSessionPlanSelected,
                    onTripDestinationSelected = onTripDestinationSelected,
                    onHubSelected = onHubSelected,
                    airports = airports,
                    todoistRepository = todoistRepository,
                    onTodoistTaskSelected = onTodoistTaskSelected,
                    onOpenTodoistSettings = onOpenTodoistSettings,
                    onPreflightCheckChanged = onPreflightCheckChanged,
                    onContinueToSeatSelection = {
                        ritualForward = true
                        onContinueToSeatSelection()
                    },
                    onBackToManifest = {
                        ritualForward = false
                        onBackToManifest()
                    },
                    onContinue = {
                        ritualForward = true
                        onContinue()
                    },
                )
                BookingRitualSurface.BoardingPass -> {
                    if (boardingPass != null) {
                        BoardingPassPreview(
                            boardingPass = boardingPass,
                            boardingPassThemeId = boardingPassThemeId,
                            onBackToReview = {
                                ritualForward = false
                                onBackToReview()
                            },
                            onBoardFlight = {
                                ritualForward = true
                                onBoardFlight()
                            },
                        )
                    }
                }
                BookingRitualSurface.GateCall -> {
                    if (boardingPass != null) {
                        GateCallScreen(
                            boardingPass = boardingPass,
                            hapticsEnabled = hapticsEnabled,
                            onGateCleared = {
                                haptics.gateCleared()
                                if (soundEnabled) onGateCleared()
                            },
                            onStartFocusFlight = {
                                haptics.focusFlightStarted()
                                onStartFocusFlight(boardingPass)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckInSurface(
    uiState: BookingUiState,
    ritualForward: Boolean,
    onCancelBooking: () -> Unit,
    onSeatSelected: (String) -> Unit,
    onObjectiveChanged: (String) -> Unit,
    onFocusTagSelected: (String) -> Unit,
    onTripPresetSelected: (com.travelblock.app.domain.model.TripPreset?) -> Unit,
    onSessionPlanSelected: (SessionPlan?) -> Unit,
    onTripDestinationSelected: (Airport) -> Unit,
    onHubSelected: (HubConnection?) -> Unit,
    airports: List<Airport>,
    todoistRepository: TodoistRepository?,
    onTodoistTaskSelected: (com.travelblock.app.data.todoist.TodoistTask?) -> Unit,
    onOpenTodoistSettings: () -> Unit,
    onPreflightCheckChanged: (String, Boolean) -> Unit,
    onContinueToSeatSelection: () -> Unit,
    onBackToManifest: () -> Unit,
    onContinue: () -> Unit,
) {
    val flowStep = if (uiState.showSeatSelection) BookingFlowStep.SeatSelection else BookingFlowStep.Manifest
    val origin = requireNotNull(uiState.origin)
    val destination = requireNotNull(uiState.legDestination)

    StickyActionScaffold(
        modifier = Modifier.fillMaxSize(),
        options = StickyActionListOptions(horizontalPadding = 24.dp),
        stickyAction = { BookingStickyActions(uiState, onContinueToSeatSelection, onCancelBooking, onContinue, onBackToManifest) },
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            BookingProgressRail(currentStep = flowStep)
        }
        item {
            CheckInPhaseHeader(uiState)
        }
        item {
            CompactRouteDocket(
                route = DocketRoute(origin.displayCode, destination.displayCode),
                flight = DocketFlightDetails(
                    durationMinutes = uiState.durationMinutes,
                    distanceMiles = uiState.distanceMiles,
                    flightNumber = uiState.flightNumber,
                    gate = uiState.gate,
                    boardingGroup = uiState.boardingGroup,
                ),
            )
        }
        item { TripRouteLine(uiState.tripStops) }
        if (uiState.showTripCard) {
            item {
                TripPlannerSection(
                    uiState,
                    origin,
                    airports,
                    TripPlannerActions(onTripPresetSelected, onSessionPlanSelected, onTripDestinationSelected, onHubSelected),
                )
            }
        }
        stepSceneItem(uiState, origin.longitude)
        item {
            BookingFlowTransition(step = flowStep, forward = ritualForward) { step ->
                BookingStepContent(
                    step,
                    uiState,
                    onSeatSelected,
                    BookingManifestActions(
                        onObjectiveChanged,
                        onFocusTagSelected,
                        todoistRepository,
                        onTodoistTaskSelected,
                        onOpenTodoistSettings,
                        onPreflightCheckChanged,
                        onContinueToSeatSelection,
                    ),
                )
            }
        }
    }
}

@Composable
private fun BookingStickyActions(
    uiState: BookingUiState,
    onCheckIn: () -> Unit,
    onCancel: () -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
) {
    if (!uiState.showSeatSelection) {
        PrimaryButton(text = "Check-in complete", onClick = onCheckIn)
        SecondaryButton(text = "Back to departures", onClick = onCancel)
        return
    }
    PrimaryButton(text = "Generate boarding pass", onClick = onContinue, enabled = uiState.selectedSeat != null)
    SecondaryButton(text = "Back to trip manifest", onClick = onBack)
}

@Composable
private fun CheckInPhaseHeader(uiState: BookingUiState) {
    val seatStep = uiState.showSeatSelection
    PhaseHeader(
        eyebrow = if (seatStep) "Cabin" else "Check-in desk",
        title = if (seatStep) "Choose your seat" else "Pack your focus",
        detail = if (seatStep) "Pick the spot you will carry into Focus Mode." else "Tell us what you are working on, so this flight has a purpose.",
    )
}

@Composable
private fun BookingStepContent(
    step: BookingFlowStep,
    uiState: BookingUiState,
    onSeatSelected: (String) -> Unit,
    actions: BookingManifestActions,
) {
    when (step) {
        BookingFlowStep.Manifest -> ManifestCard(uiState, actions)
        BookingFlowStep.SeatSelection -> CabinSeatMap(
            seats = uiState.seatOptions,
            selectedSeatId = uiState.selectedSeatId,
            seatSelectionMessage = uiState.seatSelectionMessage,
            pointsBalance = uiState.pointsBalance,
            onSeatSelected = onSeatSelected,
        )
        else -> Unit
    }
}

@Composable
private fun EmptyBookingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        EmptyState(
            content = EmptyStateContent(
                title = "Book a flight",
                message = "Choose a departure from Home to start your check-in.",
            ),
        )
    }
}

@Composable
private fun BoardingPassPreview(
    boardingPass: BoardingPass,
    boardingPassThemeId: String,
    onBackToReview: () -> Unit,
    onBoardFlight: () -> Unit,
) {
    StickyActionScaffold(
        modifier = Modifier.fillMaxSize(),
        options = StickyActionListOptions(horizontalPadding = 20.dp),
        stickyAction = {
            PrimaryButton(text = "Board flight", onClick = onBoardFlight)
            SecondaryButton(text = "Back to seats", onClick = onBackToReview)
        },
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            BookingProgressRail(currentStep = BookingFlowStep.BoardingPass)
        }
        item {
            PhaseHeader(
                eyebrow = "Document issued",
                title = "Your boarding pass",
                detail = "Gate ${boardingPass.gate}  /  ${boardingPass.boardingGroup}. Keep it handy, they will scan it at the gate.",
            )
        }
        item {
            BoardingPassReveal {
                BoardingPassCard(
                    boardingPass = boardingPass,
                    themeId = boardingPassThemeId,
                )
            }
        }
    }
}

@Composable
private fun GateCallScreen(
    boardingPass: BoardingPass,
    hapticsEnabled: Boolean,
    onGateCleared: () -> Unit,
    onStartFocusFlight: () -> Unit,
) {
    var gateStep by remember { mutableStateOf(0) }
    var gateClearedHapticFired by remember { mutableStateOf(false) }
    val cleared = gateStep >= 2

    LaunchedEffect(boardingPass.flightNumber) {
        gateStep = 0
        kotlinx.coroutines.delay(700)
        gateStep = 1
        kotlinx.coroutines.delay(700)
        gateStep = 2
    }

    LaunchedEffect(gateStep, hapticsEnabled) {
        if (gateStep >= 2 && hapticsEnabled && !gateClearedHapticFired) {
            gateClearedHapticFired = true
            onGateCleared()
        }
    }

    StickyActionScaffold(
        modifier = Modifier.fillMaxSize(),
        options = StickyActionListOptions(horizontalPadding = 20.dp),
        stickyAction = {
            PrimaryButton(
                text = if (cleared) "Start focus flight" else "Preparing cabin...",
                onClick = onStartFocusFlight,
                enabled = cleared,
            )
        },
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            BookingProgressRail(currentStep = BookingFlowStep.GateCall)
        }
        item {
            PhaseHeader(
                eyebrow = "Final call",
                title = "Gate ${boardingPass.gate} is boarding",
                detail = "Take a breath. The next tap closes the cabin doors and starts Focus Mode.",
            )
        }
        item { JetBridgeScene() }
        item {
            GateBoardingSequence(
                boarding = GateBoardingInfo(boardingPass.gate, boardingPass.boardingGroup),
                gateStep = gateStep,
            )
        }
        item {
            Text(
                text = "Next stop: ${boardingPass.legDestination.displayCode}. Timer: ${boardingPass.focusDurationMinutes} min.",
                style = MaterialTheme.typography.bodyMedium,
                color = WarmGray,
            )
        }
    }
}

/** A lined-notepad style field for the one thing you are here to do. */

/** The illustration that opens each check-in step: the check-in desk, then the cabin you will sit in. */
private fun LazyListScope.stepSceneItem(uiState: BookingUiState, departureLongitude: Double) {
    item {
        if (uiState.showSeatSelection) {
            CabinAisleScene(longitude = departureLongitude)
        } else {
            CheckInDeskScene(CheckInGateInfo(uiState.flightNumber, uiState.gate))
        }
    }
}
