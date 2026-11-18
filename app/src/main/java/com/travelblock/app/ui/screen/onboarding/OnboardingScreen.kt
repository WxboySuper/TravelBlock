package com.travelblock.app.ui.screen.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travelblock.app.data.settings.UserSettings
import com.travelblock.app.domain.engine.AirportSearchEngine
import com.travelblock.app.domain.model.Airport

private data class AirportSelectionState(
    val query: String,
    val results: List<Airport>,
    val homeAirport: Airport?,
)

@Composable
fun OnboardingScreen(
    airports: List<Airport>,
    onFinish: (homeAirport: Airport?, requestNotifications: Boolean) -> Unit,
) {
    var page by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }
    var homeAirport by remember { mutableStateOf(airports.firstOrNull { it.code == UserSettings.DefaultAirportCode }) }
    val results = remember(airports, query) { AirportSearchEngine.search(airports, query, 30) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("TRAVELBLOCK", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
            TextButton(onClick = { onFinish(homeAirport, false) }) { Text("Skip") }
        }
        Text("${page + 1} of 3", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OnboardingPageContent(
            page,
            AirportSelectionState(query, results, homeAirport),
            onQueryChanged = { query = it },
            onAirportSelected = { homeAirport = it },
        )
        if (page != 1) androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
        Button(
            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
            enabled = page != 1 || homeAirport != null,
            onClick = {
                if (page < 2) page += 1 else onFinish(homeAirport, true)
            },
        ) {
            Text(if (page == 2) "Allow notifications" else "Continue")
        }
        if (page == 2) {
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onFinish(homeAirport, false) },
            ) { Text("Not now, start using TravelBlock") }
        }
    }
}

@Composable
private fun ColumnScope.OnboardingPageContent(
    page: Int,
    airportSelection: AirportSelectionState,
    onQueryChanged: (String) -> Unit,
    onAirportSelected: (Airport) -> Unit,
) {
    when (page) {
        0 -> IntroPageContent()
        1 -> AirportPageContent(airportSelection, onQueryChanged, onAirportSelected)
        else -> ArrivalPageContent()
    }
}

@Composable
private fun IntroPageContent() {
    Text("Focus, with a flight plan.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    Text("Each focus session is a simulated flight. Choose a route, board, then stay with the cockpit until you land.", style = MaterialTheme.typography.bodyLarge)
    OnboardingNote("Your focus time becomes flight time.")
    OnboardingNote("Arrivals add airports to your journey and flights to your logbook.")
}

@Composable
private fun ColumnScope.AirportPageContent(
    selection: AirportSelectionState,
    onQueryChanged: (String) -> Unit,
    onAirportSelected: (Airport) -> Unit,
) {
    Text("Choose your home airport", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text("We'll use it as your home base. You can change it later in Settings.", style = MaterialTheme.typography.bodyMedium)
    OutlinedTextField(
        value = selection.query,
        onValueChange = onQueryChanged,
        label = { Text("Search airport, code, or city") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(selection.results, key = { it.code }) { airport ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onAirportSelected(airport) },
                colors = CardDefaults.cardColors(
                    containerColor = if (selection.homeAirport?.code == airport.code) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                ),
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("${airport.displayCode} · ${airport.city}", fontWeight = FontWeight.Bold)
                    Text(airport.name, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun ArrivalPageContent() {
    Text("Arrival updates", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text("TravelBlock can notify you when a focus flight lands, even if the app is in the background.", style = MaterialTheme.typography.bodyLarge)
    OnboardingNote("Android will ask for notification access only if you choose to turn it on.")
}

@Composable
private fun OnboardingNote(text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Text(text, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
    }
}
