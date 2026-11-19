package com.travelblock.app.ui.screen.book

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.travelblock.app.domain.engine.HubConnection
import com.travelblock.app.domain.engine.SessionPlanCalculator
import com.travelblock.app.domain.engine.TripTime
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.SessionPlan
import com.travelblock.app.ui.components.Kicker
import com.travelblock.app.ui.components.PaperCard
import com.travelblock.app.ui.components.PrimaryButton
import com.travelblock.app.ui.components.SecondaryButton
import com.travelblock.app.ui.theme.Cream
import com.travelblock.app.ui.theme.DuskBlue
import com.travelblock.app.ui.theme.Ink
import com.travelblock.app.ui.theme.Sand
import com.travelblock.app.ui.theme.WarmGray
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** One flight of a planned trip, and the break that follows it. */
data class OverviewLeg(val from: Airport, val to: Airport, val minutes: Int, val breakAfterMinutes: Int?)

/** The flights of a planned trip with their lengths and breaks, from the session plan or the hub connection. */
fun tripOverviewLegs(stops: List<Airport>, plan: SessionPlan?, hub: HubConnection?): List<OverviewLeg> {
    val breakdown = plan?.let(SessionPlanCalculator::calculate)
    val minutes = breakdown?.legDurationsMinutes ?: hub?.let { listOf(it.firstLegMinutes, it.secondLegMinutes) } ?: return emptyList()
    val breaks = breakdown?.breakDurationsMinutes ?: hub?.let { listOf(it.layoverMinutes) }.orEmpty()
    return stops.zipWithNext().zip(minutes).mapIndexed { index, (pair, legMinutes) ->
        OverviewLeg(pair.first, pair.second, legMinutes, breaks.getOrNull(index))
    }
}

/** What the traveller can do on the overview: choose how tasks are split, carry on, or leave. */
data class TripOverviewActions(
    val onTaskPerLeg: (Boolean) -> Unit = {},
    val onContinue: () -> Unit = {},
    val onCancel: () -> Unit = {},
)

/**
 * The whole trip on one page before check-in: every flight and break, when it lands, and whether one set of
 * tasks covers the trip or each flight gets its own. After this, check-in is about the first flight only.
 */
@Composable
fun TripOverviewScreen(uiState: BookingUiState, actions: TripOverviewActions) {
    val legs = remember(uiState.tripStops, uiState.sessionPlan, uiState.hub) {
        tripOverviewLegs(uiState.tripStops, uiState.sessionPlan, uiState.hub)
    }
    val total = legs.sumOf { it.minutes + (it.breakAfterMinutes ?: 0) }
    val focus = legs.sumOf { it.minutes }
    Column(Modifier.fillMaxSize().background(Cream).statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Kicker("Trip overview", color = DuskBlue)
        Text(
            uiState.tripStops.let { "${it.first().displayCode} to ${it.last().displayCode}" },
            style = MaterialTheme.typography.headlineMedium,
            color = Ink,
        )
        Text(
            "${legs.size} flights / ${formatMinutes(focus)} of focus / lands about ${landing(total)}",
            style = MaterialTheme.typography.bodyMedium,
            color = WarmGray,
        )
        PaperCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { legs.forEachIndexed { index, leg -> LegRow(index + 1, leg) } }
        }
        TaskSplitChoice(uiState.taskPerLeg, actions.onTaskPerLeg)
        Spacer(Modifier.height(4.dp))
        PrimaryButton("Continue to check-in", onClick = actions.onContinue)
        SecondaryButton("Back to departures", onClick = actions.onCancel)
    }
}

@Composable
private fun LegRow(number: Int, leg: OverviewLeg) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.size(28.dp).background(DuskBlue, CircleShape), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(number.toString(), style = MaterialTheme.typography.labelLarge, color = Cream)
            }
            Column(Modifier.weight(1f)) {
                Text("${leg.from.displayCode}  to  ${leg.to.displayCode}", style = MaterialTheme.typography.titleMedium, color = Ink)
                Text(leg.to.city.ifBlank { leg.to.name }, style = MaterialTheme.typography.bodySmall, color = WarmGray)
            }
            Text(formatMinutes(leg.minutes), style = MaterialTheme.typography.titleMedium, color = Ink)
        }
        leg.breakAfterMinutes?.let {
            Text("Layover at ${leg.to.displayCode}: ${formatMinutes(it)}", Modifier.padding(start = 40.dp).background(Sand.copy(alpha = 0.5f), CircleShape).padding(horizontal = 10.dp, vertical = 2.dp), style = MaterialTheme.typography.labelMedium, color = WarmGray)
        }
    }
}

@Composable
private fun TaskSplitChoice(perLeg: Boolean, onChange: (Boolean) -> Unit) {
    PaperCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(12.dp)) {
        Row(Modifier.fillMaxWidth().clickable { onChange(!perLeg) }, verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = !perLeg, onCheckedChange = { onChange(!it) })
            Column {
                Text("One task for the whole trip", style = MaterialTheme.typography.bodyMedium, color = Ink)
                Text(
                    if (perLeg) "Each flight asks for its own tasks before boarding." else "The tasks you pick at check-in cover every flight.",
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmGray,
                )
            }
        }
    }
}

private fun landing(totalMinutes: Int): String =
    LocalTime.now().plusMinutes((TripTime.BOARDING_BUFFER_MINUTES + totalMinutes).toLong()).format(DateTimeFormatter.ofPattern("h:mm a"))
