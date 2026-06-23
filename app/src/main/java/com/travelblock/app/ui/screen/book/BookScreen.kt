package com.travelblock.app.ui.screen.book

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelblock.app.domain.model.BoardingPass
import com.travelblock.app.domain.model.DraftFlightBooking
import com.travelblock.app.domain.model.SeatOption
import com.travelblock.app.domain.model.TicketClass
import com.travelblock.app.ui.components.BoardingPassCard
import com.travelblock.app.ui.components.BoardingPassReveal
import com.travelblock.app.ui.components.BookingFlowStep
import com.travelblock.app.ui.components.BookingFlowTransition
import com.travelblock.app.ui.components.BookingProgressRail
import com.travelblock.app.ui.components.BookingRitualSurface
import com.travelblock.app.ui.components.BookingRitualSurfaceTransition
import com.travelblock.app.ui.components.CheckInCounterHeader
import com.travelblock.app.ui.components.CompactRouteDocket
import com.travelblock.app.ui.components.EmptyState
import com.travelblock.app.ui.components.FlightProgressMapView
import com.travelblock.app.ui.components.GateBoardingSequence
import com.travelblock.app.ui.components.StickyActionScaffold
import com.travelblock.app.ui.components.TravelDayBackdrop
import com.travelblock.app.ui.components.buildFlightMapModel
import com.travelblock.app.ui.theme.CabinAmber
import com.travelblock.app.ui.theme.DeepNavy
import com.travelblock.app.ui.theme.MutedText
import com.travelblock.app.ui.theme.RunwayLine
import com.travelblock.app.ui.theme.SkyBlue
import com.travelblock.app.ui.theme.SoftSky
import com.travelblock.app.ui.theme.TerminalBlue
import com.travelblock.app.ui.theme.TicketWhite
import com.travelblock.app.ui.util.rememberTravelBlockHaptics
import kotlin.math.roundToInt

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
        onPreflightCheckChanged = viewModel::setPreflightCheck,
        onContinueToSeatSelection = viewModel::continueToSeatSelection,
        onBackToManifest = viewModel::returnToManifest,
        onContinue = viewModel::continueToBoardingPass,
        onBoardFlight = viewModel::boardFlight,
        onBackToReview = viewModel::returnToReview,
        boardingPassThemeId = boardingPassThemeId,
        onGateCleared = onGateCleared,
        onStartFocusFlight = onStartFocusFlight,
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
) {
    if (!uiState.draftAvailable) {
        EmptyBookingState()
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
    onPreflightCheckChanged: (String, Boolean) -> Unit,
    onContinueToSeatSelection: () -> Unit,
    onBackToManifest: () -> Unit,
    onContinue: () -> Unit,
) {
    val flowStep = if (uiState.showSeatSelection) {
        BookingFlowStep.SeatSelection
    } else {
        BookingFlowStep.Manifest
    }
    val origin = requireNotNull(uiState.origin)
    val destination = requireNotNull(uiState.destination)

    StickyActionScaffold(
        modifier = Modifier.fillMaxSize(),
        horizontalPadding = 16.dp,
        stickyAction = {
            if (!uiState.showSeatSelection) {
                RitualPrimaryAction(
                    label = "Check-in Complete",
                    onClick = onContinueToSeatSelection,
                )
                RitualSecondaryAction(
                    label = "Back to Departures",
                    onClick = onCancelBooking,
                    compact = true,
                )
            } else {
                RitualPrimaryAction(
                    label = "Generate Boarding Pass",
                    onClick = onContinue,
                    enabled = uiState.selectedSeat != null,
                )
                RitualSecondaryAction(
                    label = "Back to Trip Manifest",
                    onClick = onBackToManifest,
                    compact = true,
                )
            }
        },
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            BookingProgressRail(currentStep = flowStep)
        }
        item {
            TerminalPhaseHeader(
                eyebrow = if (uiState.showSeatSelection) "CABIN ASSIGNMENT" else "CHECK-IN DESK",
                title = if (uiState.showSeatSelection) "Choose your seat" else "File your trip manifest",
                detail = if (uiState.showSeatSelection) {
                    "Lock in the seat you will carry into Focus Mode."
                } else {
                    "Declare the focus cargo for this departure before boarding."
                },
            )
        }
        item {
            CompactRouteDocket(
                originCode = origin.displayCode,
                destinationCode = destination.displayCode,
                durationMinutes = uiState.durationMinutes,
                distanceMiles = uiState.distanceMiles,
                flightNumber = uiState.flightNumber,
                gate = uiState.gate,
                boardingGroup = uiState.boardingGroup,
            )
        }
        item {
            FlightProgressMapView(
                flightMap = buildFlightMapModel(
                    origin = origin,
                    destination = destination,
                    progress = 0.0,
                ),
                originCode = origin.displayCode,
                destinationCode = destination.displayCode,
                progressLabel = "${uiState.durationMinutes} min · ${uiState.distanceMiles.roundToInt()} mi",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
            )
        }
        item {
            BookingFlowTransition(step = flowStep, forward = ritualForward) { step ->
                when (step) {
                    BookingFlowStep.Manifest -> ManifestCard(
                        uiState = uiState,
                        onObjectiveChanged = onObjectiveChanged,
                        onFocusTagSelected = onFocusTagSelected,
                        onPreflightCheckChanged = onPreflightCheckChanged,
                        onSkipToSeats = onContinueToSeatSelection,
                    )
                    BookingFlowStep.SeatSelection -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SeatCabinHeader(selectedSeatId = uiState.selectedSeatId)
                        CabinSeatMap(
                            seats = uiState.seatOptions,
                            selectedSeatId = uiState.selectedSeatId,
                            seatSelectionMessage = uiState.seatSelectionMessage,
                            onSeatSelected = onSeatSelected,
                        )
                    }
                    else -> Unit
                }
            }
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
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
            title = "Book Flight",
            message = "Choose a departure from Home to start a draft booking.",
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ManifestCard(
    uiState: BookingUiState,
    onObjectiveChanged: (String) -> Unit,
    onFocusTagSelected: (String) -> Unit,
    onPreflightCheckChanged: (String, Boolean) -> Unit,
    onSkipToSeats: () -> Unit,
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, RunwayLine),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "TRIP MANIFEST",
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepNavy,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Prepare for departure. Pack your focus.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText,
                    )
                }
                Surface(
                    color = SoftSky,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RunwayLine),
                ) {
                    Text(
                        text = uiState.flightNumber,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = TerminalBlue,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            FocusCargoField(
                value = uiState.focusObjective,
                onValueChange = onObjectiveChanged,
            )
            ManifestSectionLabel("Cabin tag")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                BookingViewModel.FocusTags.forEach { tag ->
                    Surface(
                        color = if (uiState.focusTag == tag) SoftSky else Color.White,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (uiState.focusTag == tag) TerminalBlue else RunwayLine,
                        ),
                        modifier = Modifier.clickable { onFocusTagSelected(tag) },
                    ) {
                        Text(
                            text = tag,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (uiState.focusTag == tag) {
                                TerminalBlue
                            } else {
                                DeepNavy
                            },
                        )
                    }
                }
            }
            SecurityCheckGrid(
                cabinSecured = uiState.cabinSecured,
                materialsReady = uiState.materialsReady,
                distractionsStowed = uiState.distractionsStowed,
                onPreflightCheckChanged = onPreflightCheckChanged,
            )
            RitualSecondaryAction(
                label = "Skip manifest and choose seat",
                onClick = onSkipToSeats,
                compact = true,
            )
        }
    }
}

@Composable
private fun SecurityCheckGrid(
    cabinSecured: Boolean,
    materialsReady: Boolean,
    distractionsStowed: Boolean,
    onPreflightCheckChanged: (String, Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        ManifestSectionLabel("Security check")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            SecurityTile("cabin", "Cabin", "clear desk", cabinSecured, onPreflightCheckChanged)
            SecurityTile("materials", "Materials", "within reach", materialsReady, onPreflightCheckChanged)
            SecurityTile("distractions", "Phone", "stowed", distractionsStowed, onPreflightCheckChanged)
        }
    }
}

@Composable
private fun SecurityTile(
    key: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChanged: (String, Boolean) -> Unit,
) {
    Surface(
        modifier = Modifier
            .width(100.dp)
            .height(74.dp)
            .clickable { onCheckedChanged(key, !checked) },
        color = if (checked) SoftSky else Color.White,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (checked) TerminalBlue.copy(alpha = 0.36f) else RunwayLine,
        ),
    ) {
        Column(
            modifier = Modifier.padding(9.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = if (checked) "CLEARED" else "PENDING",
                style = MaterialTheme.typography.labelSmall,
                color = if (checked) TerminalBlue else CabinAmber,
                fontWeight = FontWeight.Bold,
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedText,
                )
            }
        }
    }
}

@Composable
private fun CabinSeatMap(
    seats: List<SeatOption>,
    selectedSeatId: String?,
    seatSelectionMessage: String?,
    onSeatSelected: (String) -> Unit,
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, RunwayLine),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Cabin map",
                    style = MaterialTheme.typography.titleSmall,
                    color = DeepNavy,
                    fontWeight = FontWeight.SemiBold,
                )
                selectedSeatId?.let { InfoPill(it) }
            }
            SeatLegend()
            seatSelectionMessage?.let {
                Surface(
                    color = SoftSky,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RunwayLine),
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = TerminalBlue,
                    )
                }
            }
            Surface(
                color = Color(0xFFF7FBFF),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, RunwayLine),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CabinRowLabels()
                    seats.groupBy { it.label.dropLast(1).toInt() }
                        .toSortedMap()
                        .forEach { (_, rowSeats) ->
                            CabinSeatRow(
                                rowSeats = rowSeats.sortedBy { it.label.last() },
                                selectedSeatId = selectedSeatId,
                                onSeatSelected = onSeatSelected,
                            )
                        }
                }
            }
        }
    }
}

@Composable
private fun CabinRowLabels() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf("A", "B", "C").forEach { label ->
            Text(
                text = label,
                modifier = Modifier.width(40.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MutedText,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = "AISLE",
            modifier = Modifier.width(48.dp),
            style = MaterialTheme.typography.labelSmall,
            color = TerminalBlue,
            fontWeight = FontWeight.Bold,
        )
        listOf("D", "E", "F").forEach { label ->
            Text(
                text = label,
                modifier = Modifier.width(40.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MutedText,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun CabinSeatRow(
    rowSeats: List<SeatOption>,
    selectedSeatId: String?,
    onSeatSelected: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        rowSeats.forEachIndexed { index, seat ->
            if (index == 3) {
                Text(
                    text = seat.label.dropLast(1),
                    modifier = Modifier.width(48.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = CabinAmber,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            SeatButton(
                seat = seat,
                selected = seat.id == selectedSeatId,
                onSelected = { onSeatSelected(seat.id) },
            )
        }
    }
}

@Composable
private fun SeatButton(
    seat: SeatOption,
    selected: Boolean,
    onSelected: () -> Unit,
) {
    val containerColor = when {
        selected -> TerminalBlue
        !seat.isAvailable -> Color(0xFFE4EAF2)
        seat.ticketClass == TicketClass.Premium -> Color(0xFFFFF4D6)
        seat.ticketClass == TicketClass.ExtraLegroom -> Color(0xFFDDF2FF)
        else -> SoftSky
    }
    val textColor = if (selected) {
        Color.White
    } else {
        if (seat.isAvailable) DeepNavy else MutedText.copy(alpha = 0.55f)
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(topStart = 13.dp, topEnd = 13.dp, bottomStart = 7.dp, bottomEnd = 7.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (selected) TerminalBlue else RunwayLine,
        ),
        modifier = Modifier
            .padding(2.dp)
            .width(40.dp)
            .height(36.dp)
            .semantics {
                contentDescription = if (seat.isAvailable) seat.label else "${seat.label} unavailable"
            }
            .clickable(
                enabled = seat.isAvailable || selected,
                role = Role.RadioButton,
                onClick = onSelected,
            ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = seat.label.takeLast(1),
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SeatLegend() {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        SeatLegendPill("Standard", SoftSky, dark = true)
        SeatLegendPill("Extra legroom", Color(0xFFDDF2FF), dark = true)
        SeatLegendPill("Premium", Color(0xFFFFF4D6), dark = true)
        SeatLegendPill("Unavailable", Color(0xFFE4EAF2), dark = true)
    }
}

@Composable
private fun SeatLegendPill(
    label: String,
    color: Color,
    dark: Boolean,
) {
    Surface(
        color = color,
        shape = RoundedCornerShape(7.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, RunwayLine),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = DeepNavy,
            fontWeight = FontWeight.SemiBold,
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
        horizontalPadding = 16.dp,
        stickyAction = {
            RitualPrimaryAction(
                label = "Board Flight",
                onClick = onBoardFlight,
            )
            RitualSecondaryAction(
                label = "Back to Seats",
                onClick = onBackToReview,
                compact = true,
            )
        },
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            BookingProgressRail(currentStep = BookingFlowStep.BoardingPass)
        }
        item {
            TerminalPhaseHeader(
                eyebrow = "DOCUMENT ISSUED",
                title = "Boarding pass ready",
                detail = "Gate ${boardingPass.gate} · ${boardingPass.boardingGroup} · scan before cabin closure.",
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
        item { Spacer(modifier = Modifier.height(12.dp)) }
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
        horizontalPadding = 16.dp,
        stickyAction = {
            RitualPrimaryAction(
                label = if (cleared) "Start Focus Flight" else "Preparing cabin...",
                onClick = onStartFocusFlight,
                enabled = cleared,
            )
        },
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            BookingProgressRail(currentStep = BookingFlowStep.GateCall)
        }
        item {
            TerminalPhaseHeader(
                eyebrow = "FINAL CALL",
                title = "Gate ${boardingPass.gate} now boarding",
                detail = "Hold this moment. The next tap closes the cabin and starts Focus Mode.",
            )
        }
        item {
            GateBoardingSequence(
                gate = boardingPass.gate,
                boardingGroup = boardingPass.boardingGroup,
                gateStep = gateStep,
            )
        }
        item {
            Text(
                text = "Next stop: ${boardingPass.destination.displayCode}. Timer: ${boardingPass.focusDurationMinutes} min.",
                style = MaterialTheme.typography.bodySmall,
                color = MutedText,
            )
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun InfoPill(text: String) {
    Surface(
        color = SoftSky,
        shape = RoundedCornerShape(999.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, RunwayLine),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun FocusCargoField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, RunwayLine),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "PACK YOUR FOCUS CARGO",
                    style = MaterialTheme.typography.labelSmall,
                    color = TerminalBlue,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${value.length}/120",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedText,
                    fontWeight = FontWeight.Medium,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(66.dp),
            ) {
                if (value.isBlank()) {
                    Text(
                        text = "Mission objective",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MutedText.copy(alpha = 0.68f),
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxSize(),
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
        }
    }
}

@Composable
private fun RitualPrimaryAction(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clickable(enabled = enabled, onClick = onClick),
        color = if (enabled) TerminalBlue else Color(0xFFB8C4CC),
        shape = RoundedCornerShape(10.dp),
        shadowElevation = if (enabled) 5.dp else 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (enabled) SkyBlue.copy(alpha = 0.55f) else Color.Transparent,
        ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun RitualSecondaryAction(
    label: String,
    onClick: () -> Unit,
    compact: Boolean = false,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (compact) 48.dp else 54.dp)
            .clickable(onClick = onClick),
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, RunwayLine),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = TerminalBlue,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun TerminalPhaseHeader(
    eyebrow: String,
    title: String,
    detail: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = eyebrow,
            style = MaterialTheme.typography.labelSmall,
            color = TerminalBlue,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodyMedium,
            color = MutedText,
        )
    }
}

@Composable
private fun ManifestSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = TerminalBlue,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun SeatCabinHeader(selectedSeatId: String?) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, RunwayLine),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "CABIN MAP",
                    style = MaterialTheme.typography.labelSmall,
                    color = TerminalBlue,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Choose your place on board",
                    style = MaterialTheme.typography.titleSmall,
                    color = DeepNavy,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Muted seats are already assigned.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText,
                )
            }
            selectedSeatId?.let {
                Surface(
                    color = SoftSky,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RunwayLine),
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = TerminalBlue,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
