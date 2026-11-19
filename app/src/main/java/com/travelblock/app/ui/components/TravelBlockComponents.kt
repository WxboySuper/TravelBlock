package com.travelblock.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.BoardingPass
import com.travelblock.app.ui.theme.Cream
import com.travelblock.app.ui.theme.DuskBlue
import com.travelblock.app.ui.theme.Ink
import com.travelblock.app.ui.theme.Lamp
import com.travelblock.app.ui.theme.Marigold
import com.travelblock.app.ui.theme.Paper
import com.travelblock.app.ui.theme.Sage
import com.travelblock.app.ui.theme.Sand
import com.travelblock.app.ui.theme.SoftSky
import com.travelblock.app.ui.theme.Terracotta
import com.travelblock.app.ui.theme.TicketWhite
import com.travelblock.app.ui.theme.WarmGray
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

data class RoutePreviewDetails(
    val originCode: String,
    val destinationCode: String,
    val distanceMiles: Double,
    val durationMinutes: Int,
)

data class EmptyStateContent(
    val title: String,
    val message: String,
    val actionLabel: String? = null,
)

private data class FocusNoteContent(val tag: String, val objective: String)
private data class AirportLabel(val code: String, val city: String)
data class MetricContent(val label: String, val value: String)

data class StickyActionListOptions(
    val horizontalPadding: Dp = 20.dp,
    val listState: LazyListState? = null,
)

private object BoardingPassThemes {
    // Soft, paper-friendly tints: the band behind the airport codes is a wash of colour, not a solid block.
    val Default = BoardingPassThemeStyle(ticketHeaderColor = Color(0xFFDCE8F2))
    val Skyline = BoardingPassThemeStyle(ticketHeaderColor = Color(0xFFBCD5EC))
    val Sunset = BoardingPassThemeStyle(ticketHeaderColor = Color(0xFFF6D9A8))

    fun fromId(id: String): BoardingPassThemeStyle {
        return when (id) {
            "boarding_pass_skyline" -> Skyline
            "boarding_pass_sunset" -> Sunset
            else -> Default
        }
    }
}

/** Fallback bottom padding for scroll content; [StickyActionScaffold] measures the real footer instead. */
val StickyActionFooterHeight = 176.dp

/**
 * A scrolling list with an action footer pinned to the bottom. The footer's real height is measured
 * so the last item always scrolls clear of it.
 */
@Composable
fun StickyActionScaffold(
    modifier: Modifier = Modifier,
    options: StickyActionListOptions = StickyActionListOptions(),
    stickyAction: @Composable () -> Unit,
    content: LazyListScope.() -> Unit,
) {
    val density = LocalDensity.current
    val listState = options.listState ?: rememberLazyListState()
    val horizontalPadding = options.horizontalPadding
    var footerHeightDp by remember { mutableIntStateOf(StickyActionFooterHeight.value.toInt()) }
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = horizontalPadding,
                end = horizontalPadding,
                bottom = (footerHeightDp + 20).dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
        StickyActionFooter(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .onSizeChanged { footerHeightDp = with(density) { it.height.toDp().value.toInt() } },
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
    Column(modifier = modifier.fillMaxWidth()) {
        // A soft fade so content dissolves into the footer instead of being cut off by a hard edge.
        Box(
            Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Cream))),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Cream)
                .navigationBarsPadding()
                .padding(horizontal = horizontalPadding)
                .padding(top = 4.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
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
                color = Ink,
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmGray,
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
    StampPill(text = "$points pts", modifier = modifier)
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
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = colors.content,
        )
    }
}

/** Simple schematic of a route (curved line with the plane on it). The live map lives in [TravelMap]. */
@Composable
fun RoutePreviewMap(
    route: RoutePreviewDetails,
    modifier: Modifier = Modifier,
    progress: Float? = null,
    dark: Boolean = false,
) {
    val clampedProgress = progress?.coerceIn(0f, 1f)
    val surfaceColor = if (dark) Color(0xFF1E2740) else Paper
    val lineColor = if (dark) Lamp else Terracotta
    val textColor = if (dark) Cream else Ink
    val mutedColor = if (dark) Sand else WarmGray
    val borderColor = if (dark) Color(0xFF34405F) else Sand

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = surfaceColor,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Kicker("Route", color = mutedColor)
                    Text(
                        text = "${route.durationMinutes} min  /  ${route.distanceMiles.roundToInt()} mi",
                        style = MaterialTheme.typography.titleSmall,
                        color = textColor,
                    )
                }
                if (clampedProgress != null) {
                    Text(
                        text = "${(clampedProgress * 100).roundToInt()}% flown",
                        style = MaterialTheme.typography.labelMedium,
                        color = lineColor,
                    )
                }
            }
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp),
            ) {
                val start = Offset(14.dp.toPx(), size.height * 0.78f)
                val control = Offset(size.width * 0.50f, -size.height * 0.15f)
                val end = Offset(size.width - 14.dp.toPx(), size.height * 0.78f)
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(start.x, start.y)
                    quadraticTo(control.x, control.y, end.x, end.y)
                }
                drawPath(
                    path = path,
                    color = lineColor.copy(alpha = 0.7f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 3.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 18f), 0f),
                    ),
                )
                drawCircle(textColor, radius = 6.dp.toPx(), center = start)
                drawCircle(textColor, radius = 6.dp.toPx(), center = end)
                val t = clampedProgress ?: 0.5f
                val x = (1 - t) * (1 - t) * start.x + 2 * (1 - t) * t * control.x + t * t * end.x
                val y = (1 - t) * (1 - t) * start.y + 2 * (1 - t) * t * control.y + t * t * end.y
                drawCircle(lineColor, radius = 11.dp.toPx(), center = Offset(x, y))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(route.originCode, style = MaterialTheme.typography.headlineSmall, color = textColor)
                Text(route.destinationCode, style = MaterialTheme.typography.headlineSmall, color = textColor)
            }
        }
    }
}

@Composable
fun EmptyState(
    content: EmptyStateContent,
    modifier: Modifier = Modifier,
    onAction: (() -> Unit)? = null,
) {
    PaperCard(modifier = modifier.fillMaxWidth(), contentPadding = PaddingValues(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = content.title,
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
            )
            Text(
                text = content.message,
                style = MaterialTheme.typography.bodyLarge,
                color = WarmGray,
            )
            if (content.actionLabel != null && onAction != null) {
                PrimaryButton(text = content.actionLabel, onClick = onAction)
            }
        }
    }
}

/**
 * A paper boarding pass: a soft-tinted header band with the airports, a perforation with notches,
 * then passenger details and a barcode. Looks like the ticket you would keep as a bookmark.
 */
@Composable
fun BoardingPassCard(
    boardingPass: BoardingPass,
    modifier: Modifier = Modifier,
    showClearedStamp: Boolean = true,
    themeId: String = "",
) {
    val theme = BoardingPassThemes.fromId(themeId)
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    Box(modifier = modifier.fillMaxWidth()) {
        BoardingPassTicket(boardingPass, theme, timeFormatter)
        if (showClearedStamp) {
            BoardingPassStatusStamp(
                text = "Ready to board",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 112.dp)
                    .rotate(-8f),
            )
        }
    }
}

@Composable
private fun BoardingPassTicket(
    boardingPass: BoardingPass,
    theme: BoardingPassThemeStyle,
    timeFormatter: DateTimeFormatter,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = TicketWhite,
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, Sand),
        shadowElevation = 6.dp,
    ) {
        Column {
            BoardingPassHeader(boardingPass, theme)
            TicketPerforation()
            BoardingPassDetails(boardingPass, timeFormatter)
        }
    }
}

@Composable
private fun BoardingPassHeader(boardingPass: BoardingPass, theme: BoardingPassThemeStyle) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.ticketHeaderColor)
            .padding(horizontal = 22.dp)
            .padding(top = 18.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "TravelBlock Air", style = MaterialTheme.typography.titleLarge, color = Ink)
            StampPill(text = boardingPass.flightNumber, color = Ink, textColor = Ink)
        }
        BoardingPassAirportRow(boardingPass)
    }
}

@Composable
private fun BoardingPassAirportRow(boardingPass: BoardingPass) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BoardingAirport(AirportLabel(boardingPass.origin.displayCode, boardingPass.origin.city), alignEnd = false)
        Icon(
            imageVector = Icons.Rounded.Flight,
            contentDescription = null,
            tint = Ink.copy(alpha = 0.7f),
            modifier = Modifier.size(28.dp).rotate(90f),
        )
        BoardingAirport(AirportLabel(boardingPass.destination.displayCode, boardingPass.destination.city), alignEnd = true)
    }
}

@Composable
private fun BoardingPassDetails(boardingPass: BoardingPass, timeFormatter: DateTimeFormatter) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .padding(top = 6.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        PassengerFlightTime(boardingPass)
        BoardingPassMetricRow(
            MetricContent("Seat", boardingPass.seat.label),
            MetricContent("Gate", boardingPass.gate),
            MetricContent("Group", boardingPass.boardingGroup.removePrefix("Group ").ifBlank { boardingPass.boardingGroup }),
        )
        BoardingPassMetricRow(
            MetricContent("Depart", boardingPass.departureTime.format(timeFormatter)),
            MetricContent("Arrive", boardingPass.arrivalTime.format(timeFormatter)),
            MetricContent("Distance", "${boardingPass.distanceMiles.roundToInt()} mi"),
        )
        if (hasFocusDetails(boardingPass)) {
            FocusNote(FocusNoteContent(boardingPass.focusTag, boardingPass.focusObjective))
        }
        BoardingPassBarcode(flightNumber = boardingPass.flightNumber)
    }
}

@Composable
private fun PassengerFlightTime(boardingPass: BoardingPass) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Kicker("Passenger")
            Text("Focus Traveler", style = MaterialTheme.typography.titleLarge, color = Ink)
        }
        Column(horizontalAlignment = Alignment.End) {
            Kicker("Flight time")
            Text(
                "${boardingPass.focusDurationMinutes} min",
                style = MaterialTheme.typography.titleLarge,
                color = DuskBlue,
            )
        }
    }
}

@Composable
private fun BoardingPassMetricRow(first: MetricContent, second: MetricContent, third: MetricContent) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TravelMetric(first, Modifier.weight(1f))
        TravelMetric(second, Modifier.weight(1f))
        TravelMetric(third, Modifier.weight(1f))
    }
}

private fun hasFocusDetails(boardingPass: BoardingPass): Boolean =
    boardingPass.focusTag.isNotBlank() || boardingPass.focusObjective.isNotBlank()

@Composable
private fun FocusNote(content: FocusNoteContent) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Cream,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Sand),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Kicker(if (content.tag.isNotBlank()) "Studying: ${content.tag}" else "Focus cargo")
            if (content.objective.isNotBlank()) {
                Text(content.objective, style = MaterialTheme.typography.bodyMedium, color = Ink)
            }
        }
    }
}

/** Dashed tear-off line with a half-circle cut-out on each edge, like a real ticket stub. */
@Composable
private fun TicketPerforation() {
    val cutout = MaterialTheme.colorScheme.background
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp),
    ) {
        val r = 12.dp.toPx()
        drawLine(
            color = Sand,
            start = Offset(r + 8.dp.toPx(), size.height / 2),
            end = Offset(size.width - r - 8.dp.toPx(), size.height / 2),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
        )
        drawCircle(cutout, radius = r, center = Offset(0f, size.height / 2))
        drawCircle(cutout, radius = r, center = Offset(size.width, size.height / 2))
    }
}

/** A deterministic barcode (same flight, same bars) drawn in bars of varying width. */
@Composable
private fun BoardingPassBarcode(flightNumber: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
        ) {
            var seed = flightNumber.fold(17) { acc, c -> acc * 31 + c.code }
            fun next(): Int {
                seed = seed * 1103515245 + 12345
                return (seed ushr 16) and 0x7fff
            }
            var x = 0f
            val unit = 2.2.dp.toPx()
            while (x < size.width) {
                val bar = (1 + next() % 4) * unit
                val gap = (1 + next() % 3) * unit
                val w = minOf(bar, size.width - x)
                drawRect(Ink, topLeft = Offset(x, 0f), size = Size(w, size.height))
                x += bar + gap
            }
        }
        Text(
            text = "Scan at the gate  /  $flightNumber",
            style = MaterialTheme.typography.labelSmall,
            color = WarmGray,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun TravelMetric(
    metric: MetricContent,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
) {
    Column(
        modifier = modifier.widthIn(min = 64.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Kicker(metric.label)
        Text(
            text = metric.value,
            style = MaterialTheme.typography.titleMedium,
            color = if (accent) Marigold else Ink,
        )
    }
}

@Composable
private fun BoardingAirport(airport: AirportLabel, alignEnd: Boolean) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(
            text = airport.code,
            style = MaterialTheme.typography.displaySmall,
            color = Ink,
        )
        Text(
            text = airport.city,
            style = MaterialTheme.typography.bodySmall,
            color = Ink.copy(alpha = 0.72f),
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
        StatusTone.Ready, StatusTone.Success -> ChipColors(
            container = Sage.copy(alpha = 0.16f),
            content = Color(0xFF3F7A59),
            border = Sage.copy(alpha = 0.45f),
        )
        StatusTone.Warning -> ChipColors(
            container = Marigold.copy(alpha = 0.2f),
            content = Ink,
            border = Marigold.copy(alpha = 0.5f),
        )
        StatusTone.Info -> ChipColors(
            container = SoftSky,
            content = DuskBlue,
            border = Sand,
        )
    }
}

fun airportLocationLabel(airport: Airport): String {
    return listOfNotNull(
        airport.city.takeIf { it.isNotBlank() },
        airport.stateOrRegion?.takeIf { it.isNotBlank() },
    ).joinToString(", ").ifBlank { airport.country }
}

@Composable
internal fun SoftDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier, color = Sand)
}
