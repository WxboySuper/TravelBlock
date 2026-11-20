package com.travelblock.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Flight
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.travelblock.app.ui.theme.Cream
import com.travelblock.app.ui.theme.DuskBlue
import com.travelblock.app.ui.theme.Ink
import com.travelblock.app.ui.theme.Marigold
import com.travelblock.app.ui.theme.Paper
import com.travelblock.app.ui.theme.Sage
import com.travelblock.app.ui.theme.Sand
import com.travelblock.app.ui.theme.SoftSky
import com.travelblock.app.ui.theme.Terracotta
import com.travelblock.app.ui.theme.WarmGray
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

data class DocketRoute(val originCode: String, val destinationCode: String)

data class DocketFlightDetails(
    val durationMinutes: Int,
    val distanceMiles: Double,
    val flightNumber: String,
    val gate: String,
    val boardingGroup: String,
)

data class GateBoardingInfo(val gate: String, val boardingGroup: String)

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

/**
 * A calm "travel day" progress line: four stops (check-in, seat, pass, board) with a small plane
 * sitting on the stop you are at. Completed stops are filled; upcoming ones are open circles.
 */
@Composable
fun BookingProgressRail(
    currentStep: BookingFlowStep,
    modifier: Modifier = Modifier,
) {
    val steps = BookingFlowStep.entries
    val currentIndex = steps.indexOf(currentStep)
    val dot = 30.dp

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(dot)) {
            Canvas(Modifier.fillMaxWidth().height(dot)) {
                val count = steps.size
                fun centerX(i: Int) = size.width * (i + 0.5f) / count
                val y = size.height / 2
                // Upcoming stretch: dotted.
                drawLine(
                    color = Sand,
                    start = Offset(centerX(0), y),
                    end = Offset(centerX(count - 1), y),
                    strokeWidth = 3.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 14f), 0f),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                )
                // Travelled stretch: solid.
                if (currentIndex > 0) {
                    drawLine(
                        color = DuskBlue,
                        start = Offset(centerX(0), y),
                        end = Offset(centerX(currentIndex), y),
                        strokeWidth = 3.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    )
                }
            }
            Row(Modifier.fillMaxWidth().height(dot)) {
                steps.forEachIndexed { index, _ ->
                    Box(Modifier.weight(1f).height(dot), contentAlignment = Alignment.Center) {
                        ProgressStop(done = index < currentIndex, active = index == currentIndex, size = dot)
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth()) {
            steps.forEachIndexed { index, step ->
                val active = index == currentIndex
                Text(
                    text = step.label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (active) Ink else WarmGray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ProgressStop(done: Boolean, active: Boolean, size: androidx.compose.ui.unit.Dp) {
    Surface(
        modifier = Modifier.size(if (active) size else 22.dp),
        shape = CircleShape,
        color = when {
            done -> DuskBlue
            active -> Paper
            else -> Cream
        },
        border = BorderStroke(if (active) 2.dp else 1.5.dp, if (done || active) DuskBlue else Sand),
    ) {
        Box(contentAlignment = Alignment.Center) {
            when {
                done -> Icon(Icons.Rounded.Check, contentDescription = null, tint = Paper, modifier = Modifier.size(14.dp))
                active -> Icon(
                    Icons.Rounded.Flight,
                    contentDescription = null,
                    tint = Terracotta,
                    modifier = Modifier.size(18.dp).rotate(90f),
                )
            }
        }
    }
}

@Composable
fun CompactRouteDocket(
    route: DocketRoute,
    flight: DocketFlightDetails,
    modifier: Modifier = Modifier,
) {
    PaperCard(modifier = modifier.fillMaxWidth(), raised = true, contentPadding = PaddingValues(horizontal = 22.dp, vertical = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(route.originCode, style = MaterialTheme.typography.displayMedium, color = Ink)
                    Icon(
                        Icons.Rounded.Flight,
                        contentDescription = null,
                        tint = Terracotta,
                        modifier = Modifier.size(22.dp).rotate(90f),
                    )
                    Text(route.destinationCode, style = MaterialTheme.typography.displayMedium, color = Ink)
                }
                Text(
                    text = "${flight.durationMinutes} min  /  ${flight.distanceMiles.roundToInt()} mi  /  ${flight.flightNumber}  /  Gate ${flight.gate}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            StampPill(text = flight.boardingGroup)
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

/**
 * The gate: a big gate number, a boarding-pass scanner with a sweeping light, and three short
 * stages (scan, secure the cabin, cleared) that tick off as the ritual runs.
 */
@Composable
fun GateBoardingSequence(
    boarding: GateBoardingInfo,
    gateStep: Int,
    modifier: Modifier = Modifier,
    onGateCleared: (() -> Unit)? = null,
) {
    LaunchedEffect(gateStep) {
        if (gateStep >= 2) {
            onGateCleared?.invoke()
        }
    }
    val cleared = gateStep >= 2

    PaperCard(modifier = modifier.fillMaxWidth(), contentPadding = PaddingValues(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            GateBoardingHeader(boarding, cleared)
            GateScanner(cleared = cleared)
            GateSequenceRows(gateStep)
        }
    }
}

@Composable
private fun GateBoardingHeader(boarding: GateBoardingInfo, cleared: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Kicker("Now boarding")
            Text(text = "Gate ${boarding.gate}", style = MaterialTheme.typography.displaySmall, color = Ink)
            Text(
                text = "${boarding.boardingGroup}  /  doors close when Focus Mode starts",
                style = MaterialTheme.typography.bodySmall,
                color = WarmGray,
            )
        }
        StampPill(text = if (cleared) "Cleared" else "Scanning", color = if (cleared) Sage else Marigold)
    }
}

@Composable
private fun GateSequenceRows(gateStep: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GateSequenceRow("Scanning your boarding pass", index = 0, gateStep = gateStep)
        GateSequenceRow("Securing the cabin", index = 1, gateStep = gateStep)
        GateSequenceRow("Cleared for departure", index = 2, gateStep = gateStep)
    }
}

@Composable
private fun GateScanner(cleared: Boolean) {
    val infinite = rememberInfiniteTransition(label = "scan")
    val sweep by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scanSweep",
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(SoftSky),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(92.dp)) {
            // A miniature barcode that the scanner light passes over.
            val barTop = size.height * 0.28f
            val barHeight = size.height * 0.44f
            var x = size.width * 0.12f
            var i = 0
            while (x < size.width * 0.88f) {
                val w = (if (i % 3 == 0) 5f else 2.5f) * density
                drawRect(Ink.copy(alpha = 0.78f), topLeft = Offset(x, barTop), size = Size(w, barHeight))
                x += w + (if (i % 2 == 0) 4f else 6f) * density
                i++
            }
            if (!cleared) {
                val lineX = size.width * (0.08f + 0.84f * sweep)
                drawRect(Terracotta.copy(alpha = 0.9f), topLeft = Offset(lineX, 8.dp.toPx()), size = Size(3.dp.toPx(), size.height - 16.dp.toPx()))
            }
        }
        if (cleared) {
            Surface(shape = CircleShape, color = Sage, modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Check, contentDescription = null, tint = Paper, modifier = Modifier.size(26.dp))
                }
            }
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
    val rowAlpha by animateFloatAsState(
        targetValue = if (pending) 0.45f else 1f,
        animationSpec = tween(280),
        label = "gateRowAlpha",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(rowAlpha),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(26.dp),
            shape = CircleShape,
            color = if (completed) Sage else Paper,
            border = BorderStroke(1.5.dp, if (completed) Sage else if (active) Terracotta else Sand),
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (completed) {
                    Icon(Icons.Rounded.Check, contentDescription = null, tint = Paper, modifier = Modifier.size(16.dp))
                } else if (active) {
                    Box(Modifier.size(9.dp).clip(CircleShape).background(Terracotta))
                }
            }
        }
        Text(
            text = label,
            style = if (active) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyLarge,
            color = if (pending) WarmGray else Ink,
        )
    }
}

@Composable
fun CheckInCounterHeader(
    flightNumber: String,
    modifier: Modifier = Modifier,
) {
    PaperCard(modifier = modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Check-in counter",
                style = MaterialTheme.typography.titleMedium,
                color = DuskBlue,
            )
            StampPill(text = flightNumber)
        }
    }
}

@Composable
fun PerforatedDivider(
    modifier: Modifier = Modifier,
    color: Color = Sand,
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp),
    ) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
        )
    }
}

/** A rubber-stamp style status mark that eases onto the ticket. */
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
        targetValue = if (visible) 1f else 1.4f,
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
        color = Sage.copy(alpha = 0.14f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(2.dp, Sage),
    ) {
        Text(
            text = text.uppercase(),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF3F7A59),
        )
    }
}
