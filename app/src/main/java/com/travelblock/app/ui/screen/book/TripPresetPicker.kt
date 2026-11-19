package com.travelblock.app.ui.screen.book

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.TripPreset

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TripPresetPicker(selected: TripPreset?, onSelected: (TripPreset?) -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text("Trip length", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            FilterChip(selected == null, { onSelected(null) }, label = { Text("Single flight") })
            TripPreset.presets.forEach { preset ->
                FilterChip(
                    selected = preset == selected,
                    onClick = { onSelected(preset) },
                    label = { Text("${preset.label} · ${preset.legCount} legs") },
                )
            }
        }
    }
}

data class TripPresetSelection(
    val preset: TripPreset?,
    val destination: Airport?,
    val airports: List<Airport>,
)

@Composable
fun LongHaulDestinationPicker(
    selection: TripPresetSelection,
    onSelected: (Airport) -> Unit,
) {
    if (selection.preset == null) return
    var query by remember(selection.destination?.code) { mutableStateOf("") }
    val matches = remember(query, selection.airports) {
        selection.airports.asSequence()
            .filter(Airport::isUsableForTravelBlockRouting)
            .filter { query.isBlank() || "${it.city} ${it.displayCode} ${it.name}".contains(query, true) }
            .take(8)
            .toList()
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Final destination", style = MaterialTheme.typography.titleSmall)
        Box(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text(selection.destination?.let { "${it.city} (${it.displayCode})" } ?: "Search airports") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            DropdownMenu(expanded = query.isNotBlank(), onDismissRequest = { query = "" }) {
                matches.forEach { airport ->
                    DropdownMenuItem(
                        text = { Text("${airport.city} · ${airport.displayCode}") },
                        onClick = { onSelected(airport); query = "" },
                    )
                }
            }
        }
        Text("Each leg uses your selected focus length.", style = MaterialTheme.typography.bodySmall)
    }
}
