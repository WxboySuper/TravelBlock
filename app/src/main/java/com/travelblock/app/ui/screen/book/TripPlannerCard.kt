package com.travelblock.app.ui.screen.book

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.travelblock.app.domain.engine.HubConnection
import com.travelblock.app.ui.components.Kicker
import com.travelblock.app.ui.components.PaperCard
import com.travelblock.app.ui.theme.Ink
import com.travelblock.app.ui.theme.Paper
import com.travelblock.app.ui.theme.Sand
import com.travelblock.app.ui.theme.Terracotta
import com.travelblock.app.ui.theme.WarmGray

/** The hub connection a short trip could take, and whether it is switched on. */
data class HubOption(val options: List<HubConnection>, val selected: HubConnection?)

/**
 * The trip decision at check-in: a flight can connect through a major hub, and the card shows what that adds. Every
 * flight takes as long as its distance says; longer trips are planned from Home, where routes are picked to fit the time.
 */
@Composable
fun TripPlannerCard(hub: HubOption, onHub: (HubConnection?) -> Unit) {
    if (hub.options.isEmpty()) return
    PaperCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Kicker("Trip")
            HubToggle(hub, onHub)
        }
    }
}

/** Connect a trip through a major hub: the switch shows what it costs, and the chips let you pick which hub. */
@Composable
private fun HubToggle(option: HubOption, onChange: (HubConnection?) -> Unit) {
    val shown = option.selected ?: option.options.first()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Connect through ${shown.hub.city.ifBlank { shown.hub.displayCode }} (${shown.hub.displayCode})", style = MaterialTheme.typography.bodyMedium, color = Ink)
                Text(
                    "Adds ${shown.extraMinutes} min: ${shown.firstLegMinutes} + ${shown.secondLegMinutes} min of flying and a ${shown.layoverMinutes} min layover",
                    style = MaterialTheme.typography.bodySmall, color = WarmGray,
                )
            }
            Switch(option.selected != null, { onChange(shown.takeIf { _ -> it }) })
        }
        if (option.selected != null && option.options.size > 1) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(option.options) { choice ->
                    TripChip(choice.hub.displayCode, choice.hub.code == option.selected.hub.code) { onChange(choice) }
                }
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

internal fun formatMinutes(minutes: Int): String = when {
    minutes < 60 -> "$minutes min"
    minutes % 60 == 0 -> "${minutes / 60} h"
    else -> "${minutes / 60} h ${minutes % 60} min"
}
