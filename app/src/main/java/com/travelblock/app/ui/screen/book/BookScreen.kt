package com.travelblock.app.ui.screen.book

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
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
import com.travelblock.app.domain.model.TicketClass
import com.travelblock.app.ui.components.BoardingPassCard
import com.travelblock.app.ui.components.BoardingPassReveal
import com.travelblock.app.ui.components.BookingFlowStep
import com.travelblock.app.ui.components.BookingFlowTransition
import com.travelblock.app.ui.components.BookingProgressRail
import com.travelblock.app.ui.components.BookingRitualSurface
import com.travelblock.app.ui.components.BookingRitualSurfaceTransition
import com.travelblock.app.ui.components.CompactRouteDocket
import com.travelblock.app.ui.components.EmptyState
import com.travelblock.app.ui.components.GateBoardingSequence
import com.travelblock.app.ui.components.Kicker
import com.travelblock.app.ui.components.MapStop
import com.travelblock.app.ui.components.PaperCard
import com.travelblock.app.ui.components.PrimaryButton
import com.travelblock.app.ui.components.SecondaryButton
import com.travelblock.app.ui.components.SoftChip
import com.travelblock.app.ui.components.StampPill
import com.travelblock.app.ui.components.StickyActionScaffold
import com.travelblock.app.ui.components.TravelDayBackdrop
import com.travelblock.app.ui.components.TravelMap
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
        horizontalPadding = 20.dp,
        stickyAction = {
            if (!uiState.showSeatSelection) {
                PrimaryButton(text = "Check-in complete", onClick = onContinueToSeatSelection)
                SecondaryButton(text = "Back to departures", onClick = onCancelBooking)
            } else {
                PrimaryButton(
                    text = "Generate boarding pass",
                    onClick = onContinue,
                    enabled = uiState.selectedSeat != null,
                )
                SecondaryButton(text = "Back to trip manifest", onClick = onBackToManifest)
            }
        },
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            BookingProgressRail(currentStep = flowStep)
        }
        item {
            PhaseHeader(
                eyebrow = if (uiState.showSeatSelection) "Cabin" else "Check-in desk",
                title = if (uiState.showSeatSelection) "Choose your seat" else "Pack your focus",
                detail = if (uiState.showSeatSelection) {
                    "Pick the spot you will carry into Focus Mode."
                } else {
                    "Tell us what you are working on, so this flight has a purpose."
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
        if (!uiState.showSeatSelection) {
            item { RouteMapCard(origin = origin, destination = destination) }
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
                    BookingFlowStep.SeatSelection -> CabinSeatMap(
                        seats = uiState.seatOptions,
                        selectedSeatId = uiState.selectedSeatId,
                        seatSelectionMessage = uiState.seatSelectionMessage,
                        onSeatSelected = onSeatSelected,
                    )
                    else -> Unit
                }
            }
        }
    }
}

/** A modest real map of the route, shown on check-in only. Taps are swallowed; drags scroll the page. */
@Composable
private fun RouteMapCard(origin: Airport, destination: Airport) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(176.dp)
            .clip(shape)
            .border(1.dp, Sand, shape),
    ) {
        TravelMap(
            origin = MapStop(origin.displayCode, GeoPoint(origin.latitude, origin.longitude)),
            destination = MapStop(destination.displayCode, GeoPoint(destination.latitude, destination.longitude)),
            modifier = Modifier.fillMaxSize(),
        )
        // Transparent overlay so the map never steals scroll gestures from the page.
        Box(Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { } })
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
            title = "Book a flight",
            message = "Choose a departure from Home to start your check-in.",
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
    PaperCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Trip manifest", style = MaterialTheme.typography.titleLarge, color = Ink)
                    Text(
                        "What are you studying on this flight?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmGray,
                    )
                }
                StampPill(text = uiState.flightNumber)
            }
            FocusCargoField(
                value = uiState.focusObjective,
                onValueChange = onObjectiveChanged,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Kicker("Cabin tag")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BookingViewModel.FocusTags.forEach { tag ->
                        SoftChip(
                            label = tag,
                            selected = uiState.focusTag == tag,
                            onClick = { onFocusTagSelected(tag) },
                        )
                    }
                }
            }
            SecurityCheckList(
                cabinSecured = uiState.cabinSecured,
                materialsReady = uiState.materialsReady,
                distractionsStowed = uiState.distractionsStowed,
                onPreflightCheckChanged = onPreflightCheckChanged,
            )
            SecondaryButton(text = "Skip manifest and choose seat", onClick = onSkipToSeats)
        }
    }
}

@Composable
private fun SecurityCheckList(
    cabinSecured: Boolean,
    materialsReady: Boolean,
    distractionsStowed: Boolean,
    onPreflightCheckChanged: (String, Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Kicker("Before we push back")
        SecurityRow("cabin", "Desk cleared", "A tidy space makes for a calmer flight", cabinSecured, onPreflightCheckChanged)
        SecurityRow("materials", "Materials within reach", "Notes, charger, water", materialsReady, onPreflightCheckChanged)
        SecurityRow("distractions", "Phone stowed", "Face down, out of sight", distractionsStowed, onPreflightCheckChanged)
    }
}

@Composable
private fun SecurityRow(
    key: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChanged: (String, Boolean) -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .toggleable(
                value = checked,
                role = Role.Checkbox,
                onValueChange = { onCheckedChanged(key, it) },
            ),
        color = if (checked) Sage.copy(alpha = 0.12f) else Cream,
        shape = shape,
        border = BorderStroke(1.dp, if (checked) Sage.copy(alpha = 0.55f) else Sand),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(26.dp),
                shape = CircleShape,
                color = if (checked) Sage else Paper,
                border = BorderStroke(1.5.dp, if (checked) Sage else Sand),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (checked) {
                        Icon(Icons.Rounded.Check, contentDescription = null, tint = Paper, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = Ink)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = WarmGray)
            }
        }
    }
}

/** A real-cabin seat map: a fuselage outline with window markers, row numbers down the aisle, and seat-back shapes. */
@Composable
private fun CabinSeatMap(
    seats: List<SeatOption>,
    selectedSeatId: String?,
    seatSelectionMessage: String?,
    onSeatSelected: (String) -> Unit,
) {
    PaperCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Cabin map", style = MaterialTheme.typography.titleLarge, color = Ink)
                    Text("Muted seats are already taken.", style = MaterialTheme.typography.bodySmall, color = WarmGray)
                }
                selectedSeatId?.let { StampPill(text = "Seat $it", color = DuskBlue) }
            }
            SeatLegend()
            seatSelectionMessage?.let {
                Surface(
                    color = SoftSky,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Sand),
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = DuskBlue,
                    )
                }
            }
            Fuselage {
                CabinColumnLabels()
                seats.groupBy { it.label.dropLast(1).toInt() }
                    .toSortedMap()
                    .forEach { (rowNumber, rowSeats) ->
                        CabinSeatRow(
                            rowNumber = rowNumber,
                            rowSeats = rowSeats.sortedBy { it.label.last() },
                            selectedSeatId = selectedSeatId,
                            onSeatSelected = onSeatSelected,
                        )
                    }
            }
        }
    }
}

@Composable
private fun Fuselage(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Cream,
        shape = RoundedCornerShape(topStart = 72.dp, topEnd = 72.dp, bottomStart = 26.dp, bottomEnd = 26.dp),
        border = BorderStroke(1.5.dp, Sand),
    ) {
        Column(
            modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 22.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Front of cabin",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelSmall,
                color = WarmGray,
                textAlign = TextAlign.Center,
            )
            content()
        }
    }
}

@Composable
private fun CabinColumnLabels() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(WindowWidth))
        listOf("A", "B", "C").forEach { ColumnLabel(it, Modifier.weight(1f)) }
        Spacer(Modifier.weight(0.8f))
        listOf("D", "E", "F").forEach { ColumnLabel(it, Modifier.weight(1f)) }
        Spacer(Modifier.width(WindowWidth))
    }
}

@Composable
private fun ColumnLabel(label: String, modifier: Modifier) {
    Text(
        text = label,
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        color = WarmGray,
        textAlign = TextAlign.Center,
    )
}

private val WindowWidth = 6.dp

@Composable
private fun CabinSeatRow(
    rowNumber: Int,
    rowSeats: List<SeatOption>,
    selectedSeatId: String?,
    onSeatSelected: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CabinWindow()
        rowSeats.forEachIndexed { index, seat ->
            if (index == 3) {
                Text(
                    text = rowNumber.toString(),
                    modifier = Modifier.weight(0.8f),
                    style = MaterialTheme.typography.labelSmall,
                    color = Marigold,
                    textAlign = TextAlign.Center,
                )
            }
            SeatButton(
                seat = seat,
                selected = seat.id == selectedSeatId,
                onSelected = { onSeatSelected(seat.id) },
                modifier = Modifier.weight(1f),
            )
        }
        CabinWindow()
    }
}

@Composable
private fun CabinWindow() {
    Box(
        modifier = Modifier
            .width(WindowWidth)
            .height(18.dp)
            .clip(RoundedCornerShape(50))
            .background(SoftSky)
            .border(1.dp, Sand, RoundedCornerShape(50)),
    )
}

@Composable
private fun SeatButton(
    seat: SeatOption,
    selected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = when {
        selected -> DuskBlue
        !seat.isAvailable -> Sand.copy(alpha = 0.55f)
        else -> seatTint(seat.ticketClass)
    }
    val textColor = when {
        selected -> Paper
        seat.isAvailable -> Ink
        else -> WarmGray.copy(alpha = 0.6f)
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
        border = BorderStroke(1.dp, if (selected) DuskBlue else Sand),
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 1.dp)
            .height(40.dp)
            .semantics {
                contentDescription = if (seat.isAvailable) seat.label else "${seat.label} unavailable"
            }
            .toggleable(
                value = selected,
                enabled = seat.isAvailable || selected,
                role = Role.RadioButton,
                onValueChange = { onSelected() },
            ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (selected) {
                Icon(Icons.Rounded.Check, contentDescription = null, tint = Paper, modifier = Modifier.size(18.dp))
            } else {
                Text(
                    text = seat.label.takeLast(1),
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor,
                )
            }
        }
    }
}

private fun seatTint(ticketClass: TicketClass): Color = when (ticketClass) {
    TicketClass.Premium -> Color(0xFFF8E3B4)
    TicketClass.ExtraLegroom -> Color(0xFFDCEBDD)
    TicketClass.Window -> Color(0xFFD9E6F2)
    TicketClass.Standard -> SoftSky
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SeatLegend() {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        LegendItem("Standard", SoftSky)
        LegendItem("Window", seatTint(TicketClass.Window))
        LegendItem("Extra legroom", seatTint(TicketClass.ExtraLegroom))
        LegendItem("Premium", seatTint(TicketClass.Premium))
        LegendItem("Taken", Sand.copy(alpha = 0.55f))
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier
                .size(width = 16.dp, height = 14.dp)
                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 3.dp, bottomEnd = 3.dp))
                .background(color)
                .border(1.dp, Sand, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 3.dp, bottomEnd = 3.dp)),
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = WarmGray)
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
        horizontalPadding = 20.dp,
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
        horizontalPadding = 20.dp,
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
                style = MaterialTheme.typography.bodyMedium,
                color = WarmGray,
            )
        }
    }
}

/** A lined-notepad style field for the one thing you are here to do. */
@Composable
private fun FocusCargoField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Cream,
        shape = shape,
        border = BorderStroke(1.dp, Sand),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Kicker("Focus cargo", color = DuskBlue)
                Text(
                    text = "${value.length}/120",
                    style = MaterialTheme.typography.labelSmall,
                    color = WarmGray,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp),
            ) {
                if (value.isBlank()) {
                    Text(
                        text = "e.g. Finish chapter 4 notes",
                        style = MaterialTheme.typography.bodyLarge,
                        color = WarmGray.copy(alpha = 0.7f),
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxSize(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Ink),
                    cursorBrush = SolidColor(DuskBlue),
                )
            }
        }
    }
}

@Composable
private fun PhaseHeader(
    eyebrow: String,
    title: String,
    detail: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Kicker(eyebrow, color = DuskBlue)
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = Ink,
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodyMedium,
            color = WarmGray,
        )
    }
}
