package com.travelblock.app.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.travelblock.app.ui.theme.Cream
import com.travelblock.app.ui.theme.Sand
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.travelblock.app.domain.engine.AutoSessionPlan
import com.travelblock.app.domain.engine.TripSuggestion
import com.travelblock.app.domain.engine.TripWish
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.ui.components.Kicker
import com.travelblock.app.ui.components.PaperCard
import com.travelblock.app.ui.components.SoftChip
import com.travelblock.app.ui.screen.book.formatMinutes
import com.travelblock.app.ui.theme.DuskBlue
import com.travelblock.app.ui.theme.Ink
import com.travelblock.app.ui.theme.WarmGray

/** What the planner needs from the screen: ask for options, and book the one the traveller taps. */
data class TripPlannerHomeActions(
    val options: (TripWish) -> List<TripSuggestion> = { emptyList() },
    val onPlan: (TripSuggestion) -> Unit = {},
)

private val PlannerMinutes = listOf(45, 60, 120, 180, 240, 360, 480)

/**
 * "Tell us how long": pick a time and a couple of options, and the planner offers a few trips built through real
 * hubs. You choose one and review it at check-in.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TripPlannerPanel(origin: Airport?, visitedCount: Int, actions: TripPlannerHomeActions) {
    var minutes by remember { mutableIntStateOf(120) }
    var hubs by remember { mutableStateOf(true) }
    var major by remember { mutableStateOf(true) }
    val options = remember(minutes, hubs, major, origin?.code, visitedCount) { actions.options(TripWish(minutes, hubs, major)) }
    PaperCard(Modifier.padding(horizontal = 14.dp).fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Kicker("Trip planner", color = DuskBlue)
            Text("How long do you want to focus?", style = MaterialTheme.typography.titleLarge, color = Ink)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PlannerMinutes.forEach { SoftChip(formatMinutes(it), selected = it == minutes, onClick = { minutes = it }) }
            }
            PlannerCheck("Connect through hubs", "A small airport flies through its regional hub first", hubs) { hubs = it }
            PlannerCheck("Major airports only", "Pick a big-city destination", major) { major = it }
            if (options.isEmpty()) {
                Text("Nothing fits that time from here. Try a longer one, or untick major airports.", style = MaterialTheme.typography.bodyMedium, color = WarmGray)
            }
            options.forEach { OptionCard(it) { actions.onPlan(it) } }
        }
    }
}

@Composable
private fun PlannerCheck(title: String, detail: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onChange(!checked) }, verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked, onCheckedChange = onChange)
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = Ink)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = WarmGray)
        }
    }
}

/** One trip on offer: every airport on the way, where it ends and what it asks of you. Tap to book it. */
@Composable
private fun OptionCard(suggestion: TripSuggestion, onChoose: () -> Unit) {
    val route = suggestion.route
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Cream).border(1.dp, Sand, RoundedCornerShape(16.dp))
            .clickable(onClick = onChoose).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(route.destination.city.ifBlank { route.destination.name }, style = MaterialTheme.typography.titleMedium, color = Ink)
            if (suggestion.isNew) NewPlaceTag()
        }
        Text(suggestion.stops.joinToString("  to  ") { it.displayCode }, style = MaterialTheme.typography.labelLarge, color = DuskBlue)
        Text(planSummary(suggestion), style = MaterialTheme.typography.bodySmall, color = WarmGray)
    }
}

private fun planSummary(suggestion: TripSuggestion): String {
    val focus = formatMinutes(suggestion.focusMinutes)
    val auto = AutoSessionPlan.summary(suggestion.focusMinutes)
    val hub = suggestion.hub
    return when {
        auto != null -> "$focus of focus in ${auto.legCount} flights with ${formatMinutes(auto.breakMinutes)} of breaks"
        hub != null -> "$focus of flying plus a ${hub.layoverMinutes} min layover, +${hub.extraMinutes} min over flying direct"
        else -> "$focus of focus, one flight"
    }
}
