package com.travelblock.app.ui.screen.book

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.travelblock.app.domain.engine.HubConnection
import com.travelblock.app.domain.engine.SessionPlanCalculator
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.SessionBreakPlan
import com.travelblock.app.domain.model.SessionPlan
import com.travelblock.app.domain.model.SessionPlanBreakdown
import com.travelblock.app.domain.model.TripPreset
import com.travelblock.app.ui.components.Kicker
import com.travelblock.app.ui.components.PaperCard
import com.travelblock.app.ui.theme.DuskBlue
import com.travelblock.app.ui.theme.Ink
import com.travelblock.app.ui.theme.Paper
import com.travelblock.app.ui.theme.Sand
import com.travelblock.app.ui.theme.Terracotta
import com.travelblock.app.ui.theme.WarmGray
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** What the trip card shows and edits. */
data class TripPlannerState(
    val preset: TripPreset?,
    val plan: SessionPlan?,
    val destination: Airport?,
    val airports: List<Airport>,
    val hub: HubOption = HubOption(null, false),
)

/** The hub connection a short trip could take, and whether it is switched on. */
data class HubOption(val connection: HubConnection?, val enabled: Boolean)

data class TripPlannerActions(
    val onPreset: (TripPreset?) -> Unit,
    val onPlan: (SessionPlan?) -> Unit,
    val onDestination: (Airport) -> Unit,
    val onHub: (HubConnection?) -> Unit = {},
)

/** The editable numbers of a custom session. */
private data class PlanDraft(
    val total: Int = 240,
    val block: Int = 50,
    val breakMinutes: Int = 10,
    val longBreaks: Boolean = true,
) {
    fun toPlan() = SessionPlan(
        totalFocusMinutes = total,
        blockMinutes = block,
        breaks = SessionBreakPlan(breakMinutes, LONG_BREAK_MINUTES.takeIf { longBreaks }, LONG_BREAK_EVERY.takeIf { longBreaks }),
    )

    companion object {
        fun from(plan: SessionPlan?) = plan?.let {
            PlanDraft(it.totalFocusMinutes, it.blockMinutes, it.breaks.regularMinutes, it.breaks.longMinutes != null)
        } ?: PlanDraft()
    }
}

private const val LONG_BREAK_MINUTES = 20
private const val LONG_BREAK_EVERY = 4

/**
 * One card for the whole trip decision: a single flight, or a long haul you either pick from quick trips or
 * shape yourself (how long you will focus, how often you break), with a to-scale preview of the result.
 */
@Composable
fun TripPlannerCard(state: TripPlannerState, actions: TripPlannerActions) {
    // A single flight connected through a hub is still a single flight in the card; the toggle stays on screen.
    val longHaul = (state.preset != null && !state.hub.enabled) || state.plan != null
    var draft by remember { mutableStateOf(PlanDraft.from(state.plan)) }
    PaperCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Kicker("Trip")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KindTile(KindTileSpec("Single flight", "One focus session", !longHaul), Modifier.weight(1f)) { actions.onPreset(null) }
                KindTile(KindTileSpec("Long haul", "Several flights, with breaks", longHaul), Modifier.weight(1f)) {
                    if (!longHaul) actions.onPlan(draft.toPlan())
                }
            }
            if (!longHaul) state.hub.connection?.let { HubToggle(it, state.hub.enabled, actions.onHub) }
            if (longHaul) {
                QuickTrips(state, draft, actions)
                if (state.plan != null) PlanControls(draft) { draft = it; actions.onPlan(it.toPlan()) }
                TripSummary(state)
                DestinationRow(state, actions.onDestination)
            }
        }
    }
}

/** Short hops between small airports fly direct; this adds the realistic connection and shows what it costs. */
@Composable
private fun HubToggle(connection: HubConnection, enabled: Boolean, onChange: (HubConnection?) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Connect through ${connection.hub.city.ifBlank { connection.hub.displayCode }} (${connection.hub.displayCode})", style = MaterialTheme.typography.bodyMedium, color = Ink)
            Text(
                "Adds ${connection.extraMinutes} min: ${connection.firstLegMinutes} + ${connection.secondLegMinutes} min of flying and a ${connection.layoverMinutes} min layover",
                style = MaterialTheme.typography.bodySmall, color = WarmGray,
            )
        }
        Switch(enabled, { onChange(connection.takeIf { _ -> it }) })
    }
}

private data class KindTileSpec(val title: String, val subtitle: String, val selected: Boolean)

@Composable
private fun KindTile(spec: KindTileSpec, modifier: Modifier, onClick: () -> Unit) {
    val selected = spec.selected
    val border = if (selected) BorderStroke(2.dp, Terracotta) else BorderStroke(1.dp, Sand)
    Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(16.dp), color = if (selected) Paper else MaterialTheme.colorScheme.background, border = border) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(spec.title, style = MaterialTheme.typography.titleMedium, color = Ink)
            Text(spec.subtitle, style = MaterialTheme.typography.bodySmall, color = WarmGray)
        }
    }
}

@Composable
private fun QuickTrips(state: TripPlannerState, draft: PlanDraft, actions: TripPlannerActions) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Quick trips", style = MaterialTheme.typography.labelLarge, color = WarmGray)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { TripChip("Build my own", state.plan != null) { actions.onPlan(draft.toPlan()) } }
            items(TripPreset.presets) { preset ->
                TripChip("${preset.label} / ${preset.legCount}", state.plan == null && state.preset?.id == preset.id) { actions.onPreset(preset) }
            }
        }
    }
}

@Composable
private fun TripChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) Terracotta else MaterialTheme.colorScheme.background,
        border = BorderStroke(1.dp, if (selected) Terracotta else Sand),
    ) {
        Text(
            text,
            Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) Paper else Ink,
        )
    }
}

@Composable
private fun PlanControls(draft: PlanDraft, onChange: (PlanDraft) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SliderRow(SliderSpec("I will focus for", formatMinutes(draft.total), draft.total, StepRange(30..720, 15))) { onChange(draft.copy(total = it)) }
        SliderRow(SliderSpec("Break about every", formatMinutes(draft.block), draft.block, StepRange(10..180, 5))) { onChange(draft.copy(block = it)) }
        SliderRow(SliderSpec("Each break lasts", "${draft.breakMinutes} min", draft.breakMinutes, StepRange(5..30, 5))) { onChange(draft.copy(breakMinutes = it)) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Longer rest every $LONG_BREAK_EVERY blocks", style = MaterialTheme.typography.bodyMedium, color = Ink)
                Text("$LONG_BREAK_MINUTES minutes instead of the usual break", style = MaterialTheme.typography.bodySmall, color = WarmGray)
            }
            Switch(draft.longBreaks, { onChange(draft.copy(longBreaks = it)) })
        }
    }
}

private data class StepRange(val range: IntRange, val step: Int)

private data class SliderSpec(val label: String, val value: String, val current: Int, val steps: StepRange)

@Composable
private fun SliderRow(spec: SliderSpec, onChange: (Int) -> Unit) {
    val (label, value, current) = Triple(spec.label, spec.value, spec.current)
    val range = spec.steps.range
    val step = spec.steps.step
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = WarmGray)
            Text(value, style = MaterialTheme.typography.titleLarge, color = Ink)
        }
        Slider(
            value = current.toFloat(),
            onValueChange = { onChange(((it / step).toInt() * step).coerceIn(range.first, range.last)) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = (range.last - range.first) / step - 1,
            colors = SliderDefaults.colors(thumbColor = Terracotta, activeTrackColor = Terracotta, inactiveTrackColor = Sand, activeTickColor = Terracotta, inactiveTickColor = Sand),
        )
    }
}

@Composable
private fun TripSummary(state: TripPlannerState) {
    val breakdown = state.plan?.let(SessionPlanCalculator::calculate)
    if (state.plan != null && breakdown == null) {
        Text(
            "That is more than ${TripPreset.MAX_TRIP_LEGS} flights. Make the blocks longer.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error,
        )
        return
    }
    if (breakdown == null) {
        Text("${state.preset?.legCount ?: 0} flights, each as long as the focus time you picked.", style = MaterialTheme.typography.bodyMedium, color = WarmGray)
        return
    }
    TimelineBar(breakdown)
    val end = LocalTime.now().plusMinutes((breakdown.totalFocusMinutes + breakdown.totalBreakMinutes).toLong())
    Text(
        "${breakdown.legCount} flights / ${formatMinutes(breakdown.totalFocusMinutes)} focus / ${formatMinutes(breakdown.totalBreakMinutes)} of breaks",
        style = MaterialTheme.typography.titleMedium, color = Ink,
    )
    Text("You would land around ${end.format(DateTimeFormatter.ofPattern("h:mm a"))}.", style = MaterialTheme.typography.bodySmall, color = WarmGray)
}

/** Focus blocks and breaks drawn to scale, so the shape of the session is visible at a glance. */
@Composable
private fun TimelineBar(breakdown: SessionPlanBreakdown) {
    Row(Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(50)), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        breakdown.legDurationsMinutes.forEachIndexed { index, minutes ->
            Box(Modifier.weight(minutes.toFloat()).height(14.dp).background(DuskBlue))
            breakdown.breakDurationsMinutes.getOrNull(index)?.let { Box(Modifier.weight(it.toFloat()).height(14.dp).background(Sand)) }
        }
    }
}

internal fun formatMinutes(minutes: Int): String = when {
    minutes < 60 -> "$minutes min"
    minutes % 60 == 0 -> "${minutes / 60} h"
    else -> "${minutes / 60} h ${minutes % 60} min"
}
