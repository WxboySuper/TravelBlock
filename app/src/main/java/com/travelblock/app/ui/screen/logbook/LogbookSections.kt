package com.travelblock.app.ui.screen.logbook

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.FlightTakeoff
import androidx.compose.material.icons.rounded.Luggage
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.travelblock.app.domain.engine.PilotProgressionEngine
import com.travelblock.app.domain.engine.PilotSummary
import com.travelblock.app.domain.engine.WeekFlightStats
import com.travelblock.app.ui.components.Kicker
import com.travelblock.app.ui.components.MapStop
import com.travelblock.app.ui.components.MetricColumn
import com.travelblock.app.ui.components.PaperCard
import com.travelblock.app.ui.components.RouteLeg
import com.travelblock.app.ui.components.RoutesMap
import com.travelblock.app.ui.components.StampPill
import com.travelblock.app.ui.theme.DisplayFamily
import com.travelblock.app.ui.theme.DuskBlue
import com.travelblock.app.ui.theme.Ink
import com.travelblock.app.ui.theme.Marigold
import com.travelblock.app.ui.theme.Paper
import com.travelblock.app.ui.theme.Sage
import com.travelblock.app.ui.theme.Sand
import com.travelblock.app.ui.theme.Terracotta
import com.travelblock.app.ui.theme.WarmGray
import com.travelblock.app.domain.model.GeoPoint
import kotlin.math.roundToInt

/** "9h 30m" style label that never rounds a partial hour down to zero. */
internal fun formatHoursRemaining(hours: Double): String {
    val totalMinutes = kotlin.math.ceil(hours * 60.0).toInt()
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return when {
        h == 0 -> "${m}m"
        m == 0 -> "${h}h"
        else -> "${h}h ${m}m"
    }
}

@Composable
internal fun SectionTitle(title: String, subtitle: String? = null) {
    Column(Modifier.padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = Ink)
        subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = WarmGray) }
    }
}

// ---------- Pilot rank ----------

@Composable
internal fun PilotRankCard(pilot: PilotSummary) {
    PaperCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Kicker("Pilot rank")
            Text(pilot.rank.rank.title, style = MaterialTheme.typography.headlineMedium, color = Ink)
            val next = pilot.rank.nextRank
            if (next != null) {
                ProgressBar(pilot.rank.progress)
                Text(
                    "${formatHoursRemaining(pilot.rank.hoursRemaining)} of focus to reach ${next.title}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmGray,
                )
            } else {
                Text("You've reached the top rank. Safe travels, captain.", style = MaterialTheme.typography.bodyMedium, color = WarmGray)
            }
        }
    }
}

@Composable
private fun ProgressBar(progress: Float) {
    Box(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(Sand)) {
        Box(
            Modifier
                .fillMaxWidth(progress.coerceIn(0.04f, 1f))
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Marigold),
        )
    }
}

// ---------- Streak and week ----------

@Composable
internal fun StreakAndWeekRow(pilot: PilotSummary) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatCard("Day streak", "${pilot.currentStreak}", "Best ${pilot.bestStreak}", Modifier.weight(1f))
        StatCard("This week", "${pilot.weekly.flights}", weekLine(pilot.weekly), Modifier.weight(1f))
    }
}

private fun weekLine(week: WeekFlightStats): String =
    "${"%.1f".format(week.hours)} h  /  ${week.distanceMiles.roundToInt()} mi"

@Composable
private fun StatCard(label: String, value: String, note: String, modifier: Modifier = Modifier) {
    PaperCard(modifier, contentPadding = PaddingValues(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Kicker(label)
            Text(value, style = MaterialTheme.typography.displaySmall, color = DuskBlue)
            Text(note, style = MaterialTheme.typography.bodySmall, color = WarmGray)
        }
    }
}

// ---------- Where you've been ----------

@Composable
internal fun WhereYouveBeenCard(legs: List<RouteLeg>) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Paper,
        border = BorderStroke(1.dp, Sand),
        modifier = Modifier.fillMaxWidth().height(250.dp),
    ) {
        if (legs.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    "Your map fills in with every flight you complete.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmGray,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            RoutesMap(legs, Modifier.fillMaxWidth().height(250.dp))
        }
    }
}

/** Turns completed log entries into map legs, skipping any airport we cannot place. */
internal fun legsFor(entries: List<LogbookEntryUiModel>, lookup: AirportLookup?): List<RouteLeg> {
    if (lookup == null) return emptyList()
    return entries.filter { it.isCompleted }.mapNotNull { entry ->
        val from = lookup(entry.originCode)
        val to = lookup(entry.destinationCode)
        if (from == null || to == null || !from.hasValidCoordinates || !to.hasValidCoordinates) {
            null
        } else {
            RouteLeg(
                MapStop(from.displayCode, GeoPoint(from.latitude, from.longitude)),
                MapStop(to.displayCode, GeoPoint(to.latitude, to.longitude)),
            )
        }
    }
}

// ---------- Totals ----------

@Composable
internal fun LifetimeTotalsCard(summary: LogbookSummaryUiModel) {
    PaperCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Kicker("Lifetime flight totals")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(26.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                MetricColumn("Flights", summary.totalFlightsLabel)
                MetricColumn("Landed", summary.completedFlightsLabel)
                MetricColumn("Diverted", summary.divertedFlightsLabel)
                MetricColumn("Focus", summary.totalFocusTimeLabel)
                MetricColumn("Miles", summary.totalMilesLabel)
                MetricColumn("Points", summary.totalPointsLabel)
            }
        }
    }
}

// ---------- Passport ----------

private const val MaxStamps = 24
private val StampInks = listOf(DuskBlue, Terracotta, Sage)

@Composable
internal fun PassportCard(airports: List<String>) {
    PaperCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Kicker("Airport passport")
            if (airports.isEmpty()) {
                Text("No stamps yet. Land your first flight to get one.", style = MaterialTheme.typography.bodyMedium, color = WarmGray)
            } else {
                Text("${airports.size} ${if (airports.size == 1) "airport" else "airports"} stamped", style = MaterialTheme.typography.titleMedium, color = Ink)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    airports.take(MaxStamps).forEachIndexed { index, code -> PassportStamp(code, index) }
                }
                if (airports.size > MaxStamps) {
                    Text("+${airports.size - MaxStamps} more", style = MaterialTheme.typography.labelMedium, color = WarmGray)
                }
            }
        }
    }
}

@Composable
private fun PassportStamp(code: String, index: Int) {
    val ink = StampInks[index % StampInks.size]
    val round = index % 2 == 1
    val shape = if (round) CircleShape else RoundedCornerShape(10.dp)
    Box(
        Modifier
            .rotate(((index * 37) % 11 - 5).toFloat())
            .size(width = if (round) 72.dp else 80.dp, height = if (round) 72.dp else 64.dp)
            .border(BorderStroke(2.dp, ink.copy(alpha = 0.85f)), shape)
            .padding(4.dp)
            .border(BorderStroke(1.dp, ink.copy(alpha = 0.45f)), shape),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(code, fontFamily = DisplayFamily, style = MaterialTheme.typography.titleMedium, color = ink, maxLines = 1)
            Text("ARRIVED", style = MaterialTheme.typography.labelSmall, color = ink.copy(alpha = 0.7f))
        }
    }
}

// ---------- Trophy case ----------

private data class BadgeInfo(val icon: ImageVector, val hint: String)

private val BadgeCatalog = mapOf(
    "First Flight" to BadgeInfo(Icons.Rounded.FlightTakeoff, "Land a flight"),
    "Ten Flights" to BadgeInfo(Icons.Rounded.Luggage, "Land 10 flights"),
    "Red-eye" to BadgeInfo(Icons.Rounded.NightsStay, "Fly between 10pm and 5am"),
    "Transatlantic" to BadgeInfo(Icons.Rounded.Public, "Fly a 3,000+ mile route"),
    "Airport Collector" to BadgeInfo(Icons.Rounded.Explore, "Stamp 25 airports"),
    "Seven-day Streak" to BadgeInfo(Icons.Rounded.LocalFireDepartment, "Focus 7 days in a row"),
    "Clear Skies" to BadgeInfo(Icons.Rounded.WbSunny, "Land 5 flights without a diversion"),
)

@Composable
internal fun TrophyCaseCard(unlocked: Set<String>) {
    PaperCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Kicker("Trophy case")
            Text("${unlocked.size} of ${PilotProgressionEngine.badges.size} earned", style = MaterialTheme.typography.titleMedium, color = Ink)
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val tileWidth = (maxWidth - 24.dp) / 3
                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    PilotProgressionEngine.badges.forEach { badge ->
                        BadgeTile(badge, badge in unlocked, Modifier.width(tileWidth))
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeTile(name: String, earned: Boolean, modifier: Modifier = Modifier) {
    val info = BadgeCatalog[name] ?: BadgeInfo(Icons.Rounded.WorkspacePremium, "")
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier
                .size(58.dp)
                .background(if (earned) Marigold.copy(alpha = 0.25f) else Sand.copy(alpha = 0.55f), CircleShape)
                .border(BorderStroke(2.dp, if (earned) Marigold else Sand), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(info.icon, contentDescription = null, tint = if (earned) Ink else WarmGray.copy(alpha = 0.55f), modifier = Modifier.size(28.dp))
        }
        Text(
            name,
            style = MaterialTheme.typography.labelLarge,
            color = if (earned) Ink else WarmGray,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
        Text(
            if (earned) "Earned" else info.hint,
            style = MaterialTheme.typography.labelSmall,
            color = if (earned) Sage else WarmGray.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ---------- Past flights ----------

@Composable
internal fun TicketStub(entry: LogbookEntryUiModel, onClick: () -> Unit) {
    PaperCard(Modifier.fillMaxWidth(), onClick = onClick, contentPadding = PaddingValues(0.dp)) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Column(Modifier.weight(1f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Kicker("${entry.flightNumber}  /  ${entry.dateLabel}")
                Text(entry.routeLabel.replace(" -> ", "  →  "), style = MaterialTheme.typography.titleLarge, color = Ink)
                Text("${entry.durationLabel}  /  ${entry.distanceLabel}", style = MaterialTheme.typography.bodySmall, color = WarmGray)
            }
            PerforatedEdge()
            StubSide(entry)
        }
    }
}

@Composable
private fun PerforatedEdge() {
    Box(
        Modifier
            .fillMaxHeight()
            .width(2.dp)
            .drawBehind {
                drawLine(
                    color = Sand,
                    start = Offset(size.width / 2, 8f),
                    end = Offset(size.width / 2, size.height - 8f),
                    strokeWidth = 3f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)),
                )
            },
    )
}

@Composable
private fun StubSide(entry: LogbookEntryUiModel) {
    Column(
        Modifier.width(104.dp).fillMaxHeight().padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(entry.pointsLabel, style = MaterialTheme.typography.titleSmall, color = DuskBlue)
        StampPill(
            text = entry.statusLabel,
            color = if (entry.isCompleted) Sage else Terracotta,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
