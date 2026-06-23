package com.travelblock.app.ui.screen.cockpit

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelblock.app.TravelBlockContainer
import com.travelblock.app.domain.engine.FlightRewardCalculator
import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.ActiveFlightStatus
import com.travelblock.app.domain.model.ArrivalSummary
import com.travelblock.app.domain.model.DiversionSummary
import com.travelblock.app.domain.model.FlightMapModel
import com.travelblock.app.ui.components.FlightProgressMapView
import java.time.format.DateTimeFormatter
import kotlin.math.pow

private val FlightDeckBackground = Color(0xFF06111D)
private val FlightDeckPanel = Color(0xFF0D2032)
private val FlightDeckGlass = Color(0xFF123450)
private val FlightDeckLine = Color(0xFF2D5877)
private val FlightDeckText = Color(0xFFF4FAFF)
private val FlightDeckMuted = Color(0xFF9EB7C9)
private val FlightDeckBlue = Color(0xFF54C7FF)
private val FlightDeckAmber = Color(0xFFFFC35A)

@Composable
fun CockpitScreen(
    container: TravelBlockContainer,
    activeFlight: ActiveFlight?,
    onFlightArrived: (ArrivalSummary) -> Unit,
    onFlightDiverted: (DiversionSummary) -> Unit,
) {
    val viewModel: CockpitViewModel = viewModel(
        key = activeFlight?.let { "${it.flightNumber}-${it.startedAt}" } ?: "empty-cockpit",
        factory = CockpitViewModelFactory(
            activeFlight = activeFlight,
            flightLogRepository = container.flightLogRepository,
            pointsRepository = container.pointsRepository,
            settingsRepository = container.settingsRepository,
            activeFlightSessionRepository = container.activeFlightSessionRepository,
            airports = container.airportRepository.getAllAirports(),
        ),
    )

    var arrivalHandled by remember(activeFlight) { mutableStateOf(false) }
    var diversionHandled by remember(activeFlight) { mutableStateOf(false) }

    LaunchedEffect(viewModel.uiState.status, viewModel.uiState.activeFlight) {
        val flight = viewModel.uiState.activeFlight
        if (!arrivalHandled && flight != null && viewModel.uiState.status == ActiveFlightStatus.Arrived) {
            arrivalHandled = true
            onFlightArrived(
                ArrivalSummary.fromCompletedFlight(
                    flight = flight,
                    pointsEarned = FlightRewardCalculator.completedReward(flight),
                ),
            )
        }
    }
    LaunchedEffect(viewModel.uiState.status, viewModel.uiState.diversionSummary) {
        val diversionSummary = viewModel.uiState.diversionSummary
        if (!diversionHandled && diversionSummary != null && viewModel.uiState.status == ActiveFlightStatus.Diverted) {
            diversionHandled = true
            onFlightDiverted(diversionSummary)
        }
    }

    CockpitScreenContent(
        uiState = viewModel.uiState,
        onDivertRequested = viewModel::requestDivert,
        onDismissDivert = viewModel::dismissDivertDialog,
        onConfirmDivert = viewModel::confirmDivert,
    )
}

@Composable
fun CockpitScreenContent(
    uiState: CockpitUiState,
    onDivertRequested: () -> Unit,
    onDismissDivert: () -> Unit,
    onConfirmDivert: () -> Unit,
) {
    if (!uiState.hasActiveFlight) {
        EmptyFlightDeck()
        return
    }

    val flight = requireNotNull(uiState.activeFlight)
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    if (uiState.showDivertDialog) {
        AlertDialog(
            onDismissRequest = onDismissDivert,
            containerColor = FlightDeckPanel,
            titleContentColor = FlightDeckText,
            textContentColor = FlightDeckMuted,
            title = { Text("Divert flight?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${flight.origin.code} to ${flight.destination.code}")
                    Text("Focused so far: ${uiState.elapsedLabel}")
                    Text("Miles flown: ${uiState.milesFlownLabel}")
                    Text("This saves a diverted flight and reduces completion rewards.")
                }
            },
            confirmButton = {
                FlightDeckButton(label = "Divert Flight", onClick = onConfirmDivert, danger = true)
            },
            dismissButton = {
                OutlinedButton(onClick = onDismissDivert) {
                    Text("Stay Focused")
                }
            },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF071827), FlightDeckBackground, Color(0xFF03080E)),
                ),
            ),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 16.dp,
                bottom = 128.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                FlightDeckHeader(
                    status = uiState.status,
                    flightNumber = flight.flightNumber,
                    destinationCode = flight.destination.code,
                    destinationName = flight.destination.name,
                )
            }
            item {
                InFlightHero(
                    originCode = flight.origin.code,
                    destinationCode = flight.destination.code,
                    distanceMiles = flight.distanceMiles,
                    durationMinutes = flight.durationMinutes,
                    progress = uiState.progress,
                    timeRemaining = uiState.timeRemainingLabel,
                    elapsed = uiState.elapsedLabel,
                    milesFlown = uiState.milesFlownLabel,
                    milesRemaining = uiState.milesRemainingLabel,
                    eta = uiState.estimatedArrival?.format(timeFormatter) ?: "--",
                    flightMap = uiState.flightMap,
                )
            }
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    FlightInstrument("FLOWN", uiState.milesFlownLabel)
                    FlightInstrument("LEFT", uiState.milesRemainingLabel)
                    FlightInstrument("TOTAL", uiState.totalDistanceLabel)
                    FlightInstrument("SEAT", flight.seat.label)
                    FlightInstrument("ALT", "28,000 ft")
                    FlightInstrument("SPD", "360 mph")
                }
            }
            if (flight.focusObjective.isNotBlank() || flight.focusTag.isNotBlank()) {
                item {
                    FocusCargoPanel(
                        tag = flight.focusTag,
                        objective = flight.focusObjective,
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        FlightDeckTray(
            status = uiState.status,
            elapsed = uiState.elapsedLabel,
            onDivertRequested = onDivertRequested,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun FlightDeckHeader(
    status: ActiveFlightStatus,
    flightNumber: String,
    destinationCode: String,
    destinationName: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = when (status) {
                    ActiveFlightStatus.Active -> "FOCUS FLIGHT ACTIVE"
                    ActiveFlightStatus.Arrived -> "ARRIVAL SEQUENCE"
                    ActiveFlightStatus.Diverted -> "DIVERTED"
                },
                style = MaterialTheme.typography.labelSmall,
                color = FlightDeckBlue,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "Cruising to $destinationCode",
                style = MaterialTheme.typography.headlineSmall,
                color = FlightDeckText,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = destinationName,
                style = MaterialTheme.typography.bodyMedium,
                color = FlightDeckMuted,
            )
        }
        Surface(
            color = FlightDeckGlass,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, FlightDeckLine),
        ) {
            Text(
                text = flightNumber,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = FlightDeckText,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun InFlightHero(
    originCode: String,
    destinationCode: String,
    distanceMiles: Double,
    durationMinutes: Int,
    progress: Float,
    timeRemaining: String,
    elapsed: String,
    milesFlown: String,
    milesRemaining: String,
    eta: String,
    flightMap: FlightMapModel?,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = FlightDeckPanel,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, FlightDeckLine),
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CabinWindow(progress = progress)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = timeRemaining,
                    style = MaterialTheme.typography.displayLarge,
                    color = FlightDeckText,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "ETA $eta",
                    style = MaterialTheme.typography.labelLarge,
                    color = FlightDeckAmber,
                    fontWeight = FontWeight.Bold,
                )
            }
            val resolvedMap = requireNotNull(flightMap)
            FlightProgressMapView(
                flightMap = resolvedMap,
                originCode = originCode,
                destinationCode = destinationCode,
                progressLabel = "$durationMinutes min · ${distanceMiles.toInt()} mi",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CabinProgressTile("TRAVELED", milesFlown, elapsed, Modifier.weight(1f))
                CabinProgressTile("REMAINING", milesRemaining, timeRemaining, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CabinProgressTile(
    label: String,
    value: String,
    detail: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = FlightDeckGlass,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, FlightDeckLine),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = FlightDeckBlue,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = FlightDeckText,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.labelSmall,
                color = FlightDeckMuted,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun CabinWindow(progress: Float) {
    val infinite = rememberInfiniteTransition(label = "cloudDrift")
    val drift by infinite.animateFloat(
        initialValue = -0.10f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "cloudDrift",
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp),
        color = Color.Transparent,
        shape = RoundedCornerShape(86.dp),
        border = BorderStroke(10.dp, Color(0xFF263B4D)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF7FD8FF), Color(0xFF2D8BD0), Color(0xFF173E66)),
                    ),
                ),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cloud = Color.White.copy(alpha = 0.52f)
                drawCircle(cloud, radius = 22.dp.toPx(), center = Offset(size.width * drift, size.height * 0.35f))
                drawCircle(cloud.copy(alpha = 0.38f), radius = 30.dp.toPx(), center = Offset(size.width * (drift - 0.16f), size.height * 0.48f))
                drawCircle(cloud.copy(alpha = 0.30f), radius = 24.dp.toPx(), center = Offset(size.width * (drift + 0.18f), size.height * 0.56f))
                drawLine(
                    color = Color.White.copy(alpha = 0.36f),
                    start = Offset(size.width * 0.12f, size.height * 0.76f),
                    end = Offset(size.width * 0.88f, size.height * 0.58f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(18.dp),
                color = Color.White.copy(alpha = 0.14f),
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.24f)),
            ) {
                Text(
                    text = "${(progress * 100).toInt()}% ROUTE",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun CurvedRouteDisplay(
    originCode: String,
    destinationCode: String,
    progress: Float,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp),
        ) {
            val start = Offset(18.dp.toPx(), size.height * 0.72f)
            val control = Offset(size.width * 0.50f, size.height * 0.06f)
            val end = Offset(size.width - 18.dp.toPx(), size.height * 0.72f)
            val path = Path().apply {
                moveTo(start.x, start.y)
                quadraticTo(control.x, control.y, end.x, end.y)
            }
            drawPath(
                path = path,
                color = FlightDeckBlue.copy(alpha = 0.30f),
                style = Stroke(
                    width = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f),
                ),
            )
            val t = progress.coerceIn(0f, 1f)
            fun quad(a: Float, b: Float, c: Float): Float {
                return ((1 - t).pow(2) * a) + (2 * (1 - t) * t * b) + (t.pow(2) * c)
            }
            val plane = Offset(
                x = quad(start.x, control.x, end.x),
                y = quad(start.y, control.y, end.y),
            )
            drawCircle(FlightDeckAmber, radius = 6.dp.toPx(), center = start)
            drawCircle(FlightDeckText, radius = 5.dp.toPx(), center = end)
            drawCircle(FlightDeckBlue, radius = 11.dp.toPx(), center = plane)
            drawLine(
                color = FlightDeckBlue,
                start = Offset(plane.x - 18.dp.toPx(), plane.y + 7.dp.toPx()),
                end = Offset(plane.x + 18.dp.toPx(), plane.y - 7.dp.toPx()),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            RouteCode(code = originCode, label = "ORIGIN")
            RouteCode(code = destinationCode, label = "DESTINATION", alignEnd = true)
        }
    }
}

@Composable
private fun RouteCode(
    code: String,
    label: String,
    alignEnd: Boolean = false,
) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = FlightDeckMuted,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = code,
            style = MaterialTheme.typography.headlineSmall,
            color = FlightDeckText,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
private fun FlightInstrument(label: String, value: String) {
    Surface(
        modifier = Modifier.width(104.dp),
        color = FlightDeckPanel,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, FlightDeckLine),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = FlightDeckBlue,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = FlightDeckText,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun FocusCargoPanel(tag: String, objective: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = FlightDeckGlass,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, FlightDeckLine),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Text(
                text = "FOCUS CARGO",
                style = MaterialTheme.typography.labelSmall,
                color = FlightDeckBlue,
                fontWeight = FontWeight.Black,
            )
            if (tag.isNotBlank()) {
                Surface(
                    color = FlightDeckAmber.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, FlightDeckAmber.copy(alpha = 0.45f)),
                ) {
                    Text(
                        text = tag,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = FlightDeckAmber,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            if (objective.isNotBlank()) {
                Text(
                    text = objective,
                    style = MaterialTheme.typography.bodyLarge,
                    color = FlightDeckText,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun FlightDeckTray(
    status: ActiveFlightStatus,
    elapsed: String,
    onDivertRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF07111C).copy(alpha = 0.98f),
        shadowElevation = 16.dp,
        border = BorderStroke(1.dp, FlightDeckLine),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = FlightDeckBlue.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, FlightDeckBlue.copy(alpha = 0.42f)),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "NAV",
                        style = MaterialTheme.typography.labelSmall,
                        color = FlightDeckBlue,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "FLIGHT DECK",
                    style = MaterialTheme.typography.labelSmall,
                    color = FlightDeckMuted,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Elapsed $elapsed",
                    style = MaterialTheme.typography.titleSmall,
                    color = FlightDeckText,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (status == ActiveFlightStatus.Active) {
                FlightDeckButton(label = "Divert", onClick = onDivertRequested, danger = true)
            } else {
                Text(
                    text = if (status == ActiveFlightStatus.Arrived) "LANDING" else "SAVED",
                    style = MaterialTheme.typography.labelLarge,
                    color = FlightDeckAmber,
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}

@Composable
private fun FlightDeckButton(
    label: String,
    onClick: () -> Unit,
    danger: Boolean = false,
) {
    Surface(
        modifier = Modifier
            .height(44.dp)
            .clickable(onClick = onClick),
        color = if (danger) FlightDeckAmber else FlightDeckBlue,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = if (danger) Color(0xFF221600) else Color.White,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

@Composable
private fun EmptyFlightDeck() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FlightDeckBackground)
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            color = FlightDeckPanel,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, FlightDeckLine),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Flight deck empty",
                    style = MaterialTheme.typography.headlineSmall,
                    color = FlightDeckText,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Start from a boarding pass to enter Focus Mode.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = FlightDeckMuted,
                )
            }
        }
    }
}
