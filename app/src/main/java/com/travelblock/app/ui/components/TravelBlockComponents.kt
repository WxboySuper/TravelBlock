package com.travelblock.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.BoardingPass
import com.travelblock.app.ui.theme.CabinAmber
import com.travelblock.app.ui.theme.DeepNavy
import com.travelblock.app.ui.theme.MutedText
import com.travelblock.app.ui.theme.RunwayLine
import com.travelblock.app.ui.theme.SkyBlue
import com.travelblock.app.ui.theme.SoftSky
import com.travelblock.app.ui.theme.TerminalBlue
import com.travelblock.app.ui.theme.TicketWhite
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.rotate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

enum class StatusTone {
    Ready,
    Info,
    Warning,
    Success,
}

data class BoardingPassThemeStyle(
    val ticketHeaderColor: Color,
)

private object BoardingPassThemes {
    val Default = BoardingPassThemeStyle(ticketHeaderColor = SkyBlue)
    val Skyline = BoardingPassThemeStyle(ticketHeaderColor = Color(0xFF3B97DE))
    val Sunset = BoardingPassThemeStyle(ticketHeaderColor = Color(0xFFF2A447))

    fun fromId(id: String): BoardingPassThemeStyle {
        return when (id) {
            "boarding_pass_skyline" -> Skyline
            "boarding_pass_sunset" -> Sunset
            else -> Default
        }
    }
}

/** Bottom padding so scroll content clears [StickyActionFooter] above system/nav bars. */
val StickyActionFooterHeight = 176.dp

@Composable
fun StickyActionScaffold(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 20.dp,
    listState: LazyListState = rememberLazyListState(),
    stickyAction: @Composable () -> Unit,
    content: LazyListScope.() -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = horizontalPadding,
                end = horizontalPadding,
                bottom = StickyActionFooterHeight,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
        StickyActionFooter(
            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
            horizontalPadding = horizontalPadding,
            content = stickyAction,
        )
    }
}

@Composable
fun StickyActionFooter(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 20.dp,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
        shadowElevation = 8.dp,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
                .padding(top = 10.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            content()
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedText,
                )
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun PointsPill(
    points: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = SoftSky,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, RunwayLine),
    ) {
        Text(
            text = "$points pts",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun StatusChip(
    text: String,
    modifier: Modifier = Modifier,
    tone: StatusTone = StatusTone.Info,
) {
    val colors = statusColors(tone)
    Surface(
        modifier = modifier,
        color = colors.container,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, colors.border),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = colors.content,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun RoutePreviewMap(
    originCode: String,
    destinationCode: String,
    distanceMiles: Double,
    durationMinutes: Int,
    modifier: Modifier = Modifier,
    progress: Float? = null,
    dark: Boolean = false,
) {
    val clampedProgress = progress?.coerceIn(0f, 1f)
    val surfaceColor = if (dark) Color(0xFF0D2032) else Color.White
    val lineColor = if (dark) Color(0xFF54C7FF) else SkyBlue
    val textColor = if (dark) Color(0xFFF4FAFF) else DeepNavy
    val mutedColor = if (dark) Color(0xFF9EB7C9) else MutedText
    val borderColor = if (dark) Color(0xFF2D5877) else RunwayLine
    val accentColor = if (dark) CabinAmber else TerminalBlue

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = surfaceColor,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (dark) 0.dp else 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "ROUTE PREVIEW",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "$durationMinutes min · ${distanceMiles.roundToInt()} mi",
                        style = MaterialTheme.typography.titleSmall,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                    )
                }
                if (clampedProgress != null) {
                    Text(
                        text = "${(clampedProgress * 100).roundToInt()}% FLOWN",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp),
            ) {
                val start = Offset(18.dp.toPx(), size.height * 0.70f)
                val control = Offset(size.width * 0.50f, size.height * 0.10f)
                val end = Offset(size.width - 18.dp.toPx(), size.height * 0.70f)
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(start.x, start.y)
                    quadraticTo(control.x, control.y, end.x, end.y)
                }
                drawPath(
                    path = path,
                    color = lineColor.copy(alpha = 0.24f),
                    style = Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(13f, 9f), 0f),
                    ),
                )
                if (clampedProgress != null) {
                    val traveledEndX = start.x + (end.x - start.x) * clampedProgress
                    drawLine(
                        color = lineColor.copy(alpha = 0.42f),
                        start = start,
                        end = Offset(traveledEndX, size.height * 0.70f),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
                drawCircle(accentColor, radius = 6.dp.toPx(), center = start)
                drawCircle(if (dark) Color.White else TerminalBlue, radius = 5.dp.toPx(), center = end)
                val markerProgress = clampedProgress ?: 0.50f
                val marker = Offset(
                    x = start.x + (end.x - start.x) * markerProgress,
                    y = size.height * 0.70f - (kotlin.math.sin(markerProgress * Math.PI).toFloat() * size.height * 0.48f),
                )
                drawCircle(lineColor, radius = 10.dp.toPx(), center = marker)
                drawLine(
                    color = if (dark) Color.White else DeepNavy,
                    start = Offset(marker.x - 15.dp.toPx(), marker.y + 5.dp.toPx()),
                    end = Offset(marker.x + 15.dp.toPx(), marker.y - 5.dp.toPx()),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                RoutePreviewCode("ORIGIN", originCode, textColor, mutedColor)
                RoutePreviewCode("DESTINATION", destinationCode, textColor, mutedColor, alignEnd = true)
            }
        }
    }
}

@Composable
private fun RoutePreviewCode(
    label: String,
    code: String,
    textColor: Color,
    mutedColor: Color,
    alignEnd: Boolean = false,
) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = mutedColor,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = code,
            style = MaterialTheme.typography.titleLarge,
            color = textColor,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MutedText,
            )
            if (actionLabel != null && onAction != null) {
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
fun BoardingPassCard(
    boardingPass: BoardingPass,
    modifier: Modifier = Modifier,
    showClearedStamp: Boolean = true,
    themeId: String = "",
) {
    val theme = BoardingPassThemes.fromId(themeId)
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = TicketWhite,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, RunwayLine),
        shadowElevation = 10.dp,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            BoardingPassPaperTexture()
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp),
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = theme.ticketHeaderColor,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 2.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "TRAVELBLOCK AIR",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                            Surface(
                                color = Color.White.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(999.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.30f)),
                            ) {
                                Text(
                                    text = boardingPass.flightNumber,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BoardingAirport(
                                code = boardingPass.origin.code,
                                city = boardingPass.origin.city,
                                inverse = true,
                            )
                            Text(
                                text = "✈",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                            )
                            BoardingAirport(
                                code = boardingPass.destination.code,
                                city = boardingPass.destination.city,
                                inverse = true,
                            )
                        }
                    }
                }
                PerforatedDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "PASSENGER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedText,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "FOCUS TRAVELER",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TravelMetric("Seat", boardingPass.seat.label)
                    TravelMetric("Gate", boardingPass.gate)
                    TravelMetric("Boarding", boardingPass.boardingGroup)
                    TravelMetric("Depart", boardingPass.departureTime.format(timeFormatter))
                    TravelMetric("Arrive", boardingPass.arrivalTime.format(timeFormatter))
                    TravelMetric("Focus flight", "${boardingPass.focusDurationMinutes} min")
                }
                if (boardingPass.focusTag.isNotBlank() || boardingPass.focusObjective.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (boardingPass.focusTag.isNotBlank()) {
                            StatusChip(boardingPass.focusTag, tone = StatusTone.Info)
                        }
                        if (boardingPass.focusObjective.isNotBlank()) {
                            Surface(
                                color = SoftSky,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, RunwayLine),
                            ) {
                                Text(
                                    text = boardingPass.focusObjective,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TerminalBlue,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, RunwayLine),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "SCAN AT GATE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedText,
                            fontWeight = FontWeight.SemiBold,
                        )
                        BoardingPassBarcode(flightNumber = boardingPass.flightNumber)
                    }
                }
            }
            BoardingPassEdgeNotches()
            if (showClearedStamp) {
                BoardingPassStatusStamp(
                    text = "READY TO BOARD",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 52.dp, end = 16.dp)
                        .rotate(-12f),
                )
            }
        }
    }
}

@Composable
private fun BoardingPassPaperTexture() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val lineColor = SkyBlue.copy(alpha = 0.10f)
        repeat(8) { index ->
            val y = 18f + index * 52f
            drawLine(
                color = lineColor,
                start = Offset(0f, y),
                end = Offset(size.width, y + 10f),
                strokeWidth = 1f,
            )
        }
    }
}

@Composable
private fun BoardingPassEdgeNotches() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 135.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Surface(
            modifier = Modifier.size(18.dp),
            shape = RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp),
            color = MaterialTheme.colorScheme.background,
        ) {}
        Surface(
            modifier = Modifier.size(18.dp),
            shape = RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp),
            color = MaterialTheme.colorScheme.background,
        ) {}
    }
}

@Composable
private fun BoardingPassBarcode(flightNumber: String) {
    val pattern = (flightNumber + "TB").map { ((it.code % 5) + 2) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally),
    ) {
        pattern.forEach { widthUnits ->
            Surface(
                modifier = Modifier
                    .height(48.dp)
                    .widthIn(min = (widthUnits * 2.5f).dp),
                color = DeepNavy,
                shape = RoundedCornerShape(1.dp),
            ) {}
        }
    }
    Spacer(modifier = Modifier.height(2.dp))
    Text(
        text = flightNumber,
        style = MaterialTheme.typography.labelSmall,
        color = MutedText,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
fun TravelMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
) {
    Column(
        modifier = modifier.widthIn(min = 70.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MutedText,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = if (accent) CabinAmber else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun BoardingAirport(
    code: String,
    city: String,
    inverse: Boolean = false,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = code,
            style = MaterialTheme.typography.displaySmall,
            color = if (inverse) Color.White else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = city,
            style = MaterialTheme.typography.bodyMedium,
            color = if (inverse) Color.White.copy(alpha = 0.82f) else MutedText,
        )
    }
}

private data class ChipColors(
    val container: Color,
    val content: Color,
    val border: Color,
)

@Composable
private fun statusColors(tone: StatusTone): ChipColors {
    return when (tone) {
        StatusTone.Ready -> ChipColors(
            container = Color(0xFFE5F4EE),
            content = Color(0xFF287A55),
            border = Color(0xFFC9E6D9),
        )
        StatusTone.Warning -> ChipColors(
            container = CabinAmber.copy(alpha = 0.16f),
            content = CabinAmber,
            border = CabinAmber.copy(alpha = 0.32f),
        )
        StatusTone.Success -> ChipColors(
            container = Color(0xFFE5F4EE),
            content = Color(0xFF287A55),
            border = Color(0xFFC9E6D9),
        )
        StatusTone.Info -> ChipColors(
            container = SoftSky,
            content = MaterialTheme.colorScheme.primary,
            border = RunwayLine,
        )
    }
}

fun airportLocationLabel(airport: Airport): String {
    return listOfNotNull(
        airport.city.takeIf { it.isNotBlank() },
        airport.stateOrRegion?.takeIf { it.isNotBlank() },
    ).joinToString(", ").ifBlank { airport.country }
}
