package com.travelblock.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.travelblock.app.ui.theme.CabinAmber
import com.travelblock.app.ui.theme.DeepNavy
import com.travelblock.app.ui.theme.MutedText
import com.travelblock.app.ui.theme.RunwayLine
import com.travelblock.app.ui.theme.SkyBlue
import com.travelblock.app.ui.theme.SoftSky
import com.travelblock.app.ui.theme.TerminalBlue
import kotlin.math.roundToInt

enum class BookingFlowStep(val label: String, val order: Int) {
    Manifest("Check-in", 0),
    SeatSelection("Seat", 1),
    BoardingPass("Pass", 2),
    GateCall("Board", 3),
}

enum class BookingRitualSurface(val order: Int) {
    CheckIn(0),
    BoardingPass(1),
    GateCall(2),
}

private val TerminalInk = DeepNavy
private val TerminalInkSoft = TerminalBlue
private val TicketPaper = Color(0xFFFFFEFA)

@Composable
fun TravelDayBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxWidth(),
    ) {
        content()
    }
}

@Composable
fun BookingProgressRail(
    currentStep: BookingFlowStep,
    modifier: Modifier = Modifier,
) {
    val steps = BookingFlowStep.entries
    val currentIndex = steps.indexOf(currentStep)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, RunwayLine),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "TRAVEL DAY",
                    style = MaterialTheme.typography.labelSmall,
                    color = TerminalBlue,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "STEP ${currentIndex + 1}/4",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedText,
                    fontWeight = FontWeight.Bold,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                steps.forEachIndexed { index, step ->
                    val done = index < currentIndex
                    val active = index == currentIndex
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Surface(
                            modifier = Modifier.size(26.dp),
                            shape = CircleShape,
                            color = when {
                                done -> TerminalBlue
                                active -> SkyBlue
                                else -> SoftSky
                            },
                            border = BorderStroke(
                                1.dp,
                                if (active) SkyBlue else RunwayLine,
                            ),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (done) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp),
                                    )
                                } else {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) {
                                            Color.White
                                        } else {
                                            MutedText
                                        },
                                    )
                                }
                            }
                        }
                        Text(
                            text = step.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (active) TerminalBlue else MutedText,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        )
                    }
                }
            }
            LinearProgressIndicator(
                progress = { (currentIndex + 1) / steps.size.toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = SkyBlue,
                trackColor = SoftSky,
            )
        }
    }
}

@Composable
fun CompactRouteDocket(
    originCode: String,
    destinationCode: String,
    durationMinutes: Int,
    distanceMiles: Double,
    flightNumber: String,
    gate: String,
    boardingGroup: String,
    modifier: Modifier = Modifier,
) {
    val routeLine = buildString {
        append(originCode)
        append(" → ")
        append(destinationCode)
        append(" · ")
        append(durationMinutes)
        append(" min · ")
        append(distanceMiles.roundToInt())
        append(" mi · ")
        append(flightNumber)
        append(" · Gate ")
        append(gate)
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = TerminalBlue,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, TerminalBlue),
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = "DEPARTURE DOCKET",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.76f),
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = routeLine,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Surface(
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
            ) {
                Text(
                    text = boardingGroup.uppercase(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
fun BookingFlowTransition(
    step: BookingFlowStep,
    forward: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (BookingFlowStep) -> Unit,
) {
    AnimatedContent(
        targetState = step,
        modifier = modifier,
        transitionSpec = {
            val enterOffset: (Int) -> Int = { fullWidth ->
                if (forward) fullWidth / 5 else -fullWidth / 5
            }
            val exitOffset: (Int) -> Int = { fullWidth ->
                if (forward) -fullWidth / 6 else fullWidth / 6
            }
            (fadeIn(tween(280, easing = FastOutSlowInEasing)) +
                slideInHorizontally(tween(280), enterOffset)) togetherWith
                (fadeOut(tween(180)) + slideOutHorizontally(tween(180), exitOffset))
        },
        label = "bookingFlowStep",
    ) { target ->
        content(target)
    }
}

@Composable
fun BookingRitualSurfaceTransition(
    surface: BookingRitualSurface,
    forward: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (BookingRitualSurface) -> Unit,
) {
    AnimatedContent(
        targetState = surface,
        modifier = modifier,
        transitionSpec = {
            val enterOffset: (Int) -> Int = { fullWidth ->
                if (forward) fullWidth / 4 else -fullWidth / 4
            }
            val exitOffset: (Int) -> Int = { fullWidth ->
                if (forward) -fullWidth / 5 else fullWidth / 5
            }
            (fadeIn(tween(300, easing = FastOutSlowInEasing)) +
                slideInHorizontally(tween(300), enterOffset)) togetherWith
                (fadeOut(tween(220)) + slideOutHorizontally(tween(220), exitOffset))
        },
        label = "bookingRitualSurface",
    ) { target ->
        content(target)
    }
}

@Composable
fun BoardingPassReveal(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(420, easing = FastOutSlowInEasing)) +
            slideInHorizontally(tween(480, easing = FastOutSlowInEasing)) { it / 8 } +
            scaleIn(
                initialScale = 0.94f,
                animationSpec = tween(480, easing = FastOutSlowInEasing),
            ),
        exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.96f),
    ) {
        content()
    }
}

@Composable
fun GateBoardingSequence(
    gate: String,
    boardingGroup: String,
    gateStep: Int,
    modifier: Modifier = Modifier,
    onGateCleared: (() -> Unit)? = null,
) {
    val infinite = rememberInfiniteTransition(label = "scan")
    val scanOffset by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "scanOffset",
    )

    LaunchedEffect(gateStep) {
        if (gateStep >= 2) {
            onGateCleared?.invoke()
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, RunwayLine),
        shadowElevation = 3.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "GATE $gate",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TerminalInk,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "$boardingGroup now boarding",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TerminalBlue,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Cabin doors close when Focus Mode starts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText,
                    )
                }
                Surface(
                    color = SoftSky,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, RunwayLine),
                ) {
                    Text(
                        text = if (gateStep >= 2) "CLEARED" else "SCAN",
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = TerminalBlue,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(82.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SoftSky),
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val grid = TerminalBlue.copy(alpha = 0.12f)
                    repeat(5) { row ->
                        val y = (row + 1) * size.height / 6
                        drawLine(grid, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                    }
                    repeat(6) { column ->
                        val x = (column + 1) * size.width / 7
                        drawLine(grid, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                    }
                    drawRoundRect(
                        color = SkyBlue.copy(alpha = 0.72f),
                        style = Stroke(width = 2.5f),
                    )
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        text = "BOARDING PASS SCAN",
                        style = MaterialTheme.typography.labelSmall,
                        color = TerminalBlue,
                        fontWeight = FontWeight.Bold,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White),
                    )
                }
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(82.dp)
                        .offset(x = (scanOffset * maxWidth.value).dp)
                        .background(SkyBlue.copy(alpha = 0.88f)),
                )
            }
            GateSequenceRow("Scanning boarding pass", index = 0, gateStep = gateStep)
            GateSequenceRow("Securing cabin", index = 1, gateStep = gateStep)
            GateSequenceRow("Cleared for departure", index = 2, gateStep = gateStep)
        }
    }
}

@Composable
private fun GateSequenceRow(
    label: String,
    index: Int,
    gateStep: Int,
) {
    val completed = gateStep > index || (gateStep >= 2 && index == 2)
    val active = gateStep == index && !completed
    val pending = gateStep < index
    val scale by animateFloatAsState(
        targetValue = when {
            active -> 1.02f
            completed -> 1f
            else -> 0.98f
        },
        animationSpec = tween(280),
        label = "gateRowScale",
    )
    val rowAlpha by animateFloatAsState(
        targetValue = when {
            pending -> 0.42f
            else -> 1f
        },
        animationSpec = tween(280),
        label = "gateRowAlpha",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(rowAlpha),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(22.dp),
            shape = CircleShape,
            color = when {
                completed -> TerminalBlue
                active -> SkyBlue
                else -> SoftSky
            },
            border = if (active) BorderStroke(1.dp, SkyBlue) else BorderStroke(1.dp, RunwayLine),
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (completed) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                } else if (active) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                    )
                }
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = when {
                active -> FontWeight.SemiBold
                completed -> FontWeight.Medium
                else -> FontWeight.Normal
            },
            color = if (pending) MutedText else TerminalInk,
        )
    }
}

@Composable
fun CheckInCounterHeader(
    flightNumber: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, RunwayLine),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Check-in counter",
                style = MaterialTheme.typography.labelLarge,
                color = TerminalBlue,
                fontWeight = FontWeight.Bold,
            )
            Surface(
                color = SoftSky,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, RunwayLine),
            ) {
                Text(
                    text = flightNumber,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TerminalBlue,
                )
            }
        }
    }
}

@Composable
fun PerforatedDivider(
    modifier: Modifier = Modifier,
    color: Color = RunwayLine,
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp),
    ) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = 1.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
        )
    }
}

@Composable
fun BoardingPassStatusStamp(
    text: String,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.82f,
        animationSpec = tween(520, easing = FastOutSlowInEasing),
        label = "stampScale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(420),
        label = "stampAlpha",
    )
    Surface(
        modifier = modifier
            .scale(scale)
            .alpha(alpha),
        color = Color(0xFFE8F2EA),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.5.dp, Color(0xFF3D7A55)),
        shadowElevation = 2.dp,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D6B47),
        )
    }
}
