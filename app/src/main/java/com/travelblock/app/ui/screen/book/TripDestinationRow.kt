package com.travelblock.app.ui.screen.book

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.ui.theme.Ink
import com.travelblock.app.ui.theme.Sand
import com.travelblock.app.ui.theme.Terracotta
import com.travelblock.app.ui.theme.WarmGray

private const val MAX_RESULTS = 6

/** "Fly to: Tulsa (TUL)  Change": tap to search for any other airport. */
@Composable
internal fun DestinationRow(state: TripPlannerState, onSelected: (Airport) -> Unit) {
    var searching by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            onClick = { searching = !searching },
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.background,
            border = BorderStroke(1.dp, Sand),
        ) {
            Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Final destination", style = MaterialTheme.typography.labelMedium, color = WarmGray)
                    Text(state.destination?.let { "${it.city} (${it.displayCode})" } ?: "Choose an airport", style = MaterialTheme.typography.titleMedium, color = Ink)
                }
                Text(if (searching) "Close" else "Change", style = MaterialTheme.typography.labelLarge, color = Terracotta)
            }
        }
        if (searching) AirportSearch(state.airports) { onSelected(it); searching = false }
    }
}

@Composable
private fun AirportSearch(airports: List<Airport>, onPicked: (Airport) -> Unit) {
    var query by remember { mutableStateOf("") }
    val matches = remember(query, airports) {
        if (query.length < 2) emptyList() else airports.asSequence()
            .filter(Airport::isUsableForTravelBlockRouting)
            .filter { "${it.city} ${it.displayCode} ${it.name}".contains(query, ignoreCase = true) }
            .take(MAX_RESULTS)
            .toList()
    }
    OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("City or airport code") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    matches.forEach { airport ->
        Surface(onClick = { onPicked(airport) }, shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
            Text(
                "${airport.city}, ${airport.stateOrRegion ?: airport.country} / ${airport.displayCode}",
                Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyMedium, color = Ink,
            )
        }
    }
}
