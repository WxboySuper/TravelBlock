package com.travelblock.app.ui.screen.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.travelblock.app.data.settings.UserSettings
import com.travelblock.app.domain.engine.AirportSearchEngine
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.ui.components.Kicker
import com.travelblock.app.ui.components.PaperCard
import com.travelblock.app.ui.components.PrimaryButton
import com.travelblock.app.ui.scene.AirplaneWindow
import com.travelblock.app.ui.scene.ArrivalScene
import com.travelblock.app.ui.scene.ArrivalLocation
import com.travelblock.app.ui.scene.TerminalWindows
import com.travelblock.app.ui.scene.localSolarHour
import com.travelblock.app.ui.theme.Cream
import com.travelblock.app.ui.theme.DuskBlue
import com.travelblock.app.ui.theme.Ink
import com.travelblock.app.ui.theme.Sand
import com.travelblock.app.ui.theme.Terracotta
import com.travelblock.app.ui.theme.WarmGray
import java.time.Instant

private data class AirportSelectionState(
    val query: String,
    val results: List<Airport>,
    val homeAirport: Airport?,
)

private data class OnboardingCallbacks(
    val onQueryChanged: (String) -> Unit,
    val onAirportSelected: (Airport) -> Unit,
)

private const val LAST_PAGE = 2

@Composable
fun OnboardingScreen(
    airports: List<Airport>,
    onFinish: (homeAirport: Airport?, requestNotifications: Boolean) -> Unit,
) {
    var page by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }
    var homeAirport by remember { mutableStateOf(airports.firstOrNull { it.code == UserSettings.DefaultAirportCode }) }
    val results = remember(airports, query, homeAirport) {
        val found = AirportSearchEngine.search(airports, query, 30)
        val pinned = homeAirport?.takeIf { query.isBlank() }
        if (pinned == null) found else listOf(pinned) + found.filter { it.code != pinned.code }
    }

    Column(Modifier.fillMaxSize().background(Cream)) {
        OnboardingScene(page, homeAirport, onSkip = { onFinish(homeAirport, false) })
        Column(
            Modifier.weight(1f).fillMaxWidth().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PageDots(page)
            OnboardingPageContent(
                page,
                AirportSelectionState(query, results, homeAirport),
                callbacks = OnboardingCallbacks(
                    onQueryChanged = { query = it },
                    onAirportSelected = { homeAirport = it },
                ),
                modifier = Modifier.weight(1f),
            )
            OnboardingActions(page, canContinue = page != 1 || homeAirport != null, onNext = { page += 1 }, onFinish = { notify -> onFinish(homeAirport, notify) })
        }
    }
}

@Composable
private fun OnboardingScene(page: Int, homeAirport: Airport?, onSkip: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(280.dp)) {
        val hour = remember { localSolarHour(Instant.now(), homeAirport?.longitude ?: -97.0) }
        when (page) {
            0 -> TerminalWindows(hour, Modifier.fillMaxSize())
            1 -> ArrivalScene(
                ArrivalLocation(homeAirport?.displayCode ?: "HOME", homeAirport?.longitude ?: -97.0),
                Modifier.fillMaxSize(),
            )
            else -> AirplaneWindow(hour, Modifier.fillMaxSize().padding(start = 56.dp, end = 56.dp, top = 70.dp, bottom = 14.dp))
        }
        val onScene = if (page == LAST_PAGE) Ink else Color.White
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 22.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("TRAVELBLOCK", style = MaterialTheme.typography.labelLarge, color = onScene)
            Text(
                "Skip",
                style = MaterialTheme.typography.labelLarge,
                color = onScene,
                modifier = Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onSkip).padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun PageDots(page: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(LAST_PAGE + 1) { index ->
            Box(Modifier.size(if (index == page) 22.dp else 8.dp, 8.dp).clip(CircleShape).background(if (index == page) Terracotta else Sand))
        }
        Kicker("  Step ${page + 1} of ${LAST_PAGE + 1}")
    }
}

@Composable
private fun OnboardingActions(page: Int, canContinue: Boolean, onNext: () -> Unit, onFinish: (Boolean) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        PrimaryButton(
            text = if (page == LAST_PAGE) "Allow notifications" else "Continue",
            enabled = canContinue,
            onClick = { if (page < LAST_PAGE) onNext() else onFinish(true) },
        )
        if (page == LAST_PAGE) {
            Text(
                "Not now, start using TravelBlock",
                style = MaterialTheme.typography.labelLarge,
                color = DuskBlue,
                modifier = Modifier.align(Alignment.CenterHorizontally).clip(RoundedCornerShape(50)).clickable { onFinish(false) }.padding(12.dp),
            )
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: Int,
    airportSelection: AirportSelectionState,
    callbacks: OnboardingCallbacks,
    modifier: Modifier = Modifier,
) {
    when (page) {
        0 -> IntroPageContent(modifier)
        1 -> AirportPageContent(airportSelection, callbacks.onQueryChanged, callbacks.onAirportSelected, modifier)
        else -> ArrivalPageContent(modifier)
    }
}

@Composable
private fun IntroPageContent(modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Focus, with a flight plan.", style = MaterialTheme.typography.headlineLarge, color = Ink)
        Text("Every focus session is a real-feeling trip. Check in, find your seat, board, and stay at your window until you land.", style = MaterialTheme.typography.bodyLarge, color = WarmGray)
        Box(Modifier.height(4.dp))
        OnboardingPoint("Your focus time becomes flight time.")
        OnboardingPoint("Every landing adds an airport to your passport and a flight to your logbook.")
    }
}

@Composable
private fun AirportPageContent(
    selection: AirportSelectionState,
    onQueryChanged: (String) -> Unit,
    onAirportSelected: (Airport) -> Unit,
    modifier: Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Where is home?", style = MaterialTheme.typography.headlineMedium, color = Ink)
        Text("Pick your home airport. You can change it any time in Settings.", style = MaterialTheme.typography.bodyMedium, color = WarmGray)
        OutlinedTextField(
            value = selection.query,
            onValueChange = onQueryChanged,
            label = { Text("Search airport, code or city") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
        )
        AirportResults(selection, onAirportSelected, Modifier.weight(1f))
    }
}

@Composable
private fun AirportResults(selection: AirportSelectionState, onAirportSelected: (Airport) -> Unit, modifier: Modifier) {
    LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(selection.results, key = { it.code }) { airport ->
            PaperCard(
                Modifier.fillMaxWidth(),
                onClick = { onAirportSelected(airport) },
                selected = selection.homeAirport?.code == airport.code,
                contentPadding = PaddingValues(14.dp),
            ) {
                Column {
                    Text("${airport.displayCode}  /  ${airport.city}", style = MaterialTheme.typography.titleMedium, color = Ink)
                    Text(airport.name, style = MaterialTheme.typography.bodySmall, color = WarmGray, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun ArrivalPageContent(modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("A tap on the shoulder when you land", style = MaterialTheme.typography.headlineMedium, color = Ink)
        Text("TravelBlock can let you know when your focus flight touches down, even if you have switched to another app.", style = MaterialTheme.typography.bodyLarge, color = WarmGray)
        Box(Modifier.height(4.dp))
        OnboardingPoint("Android only asks if you choose to turn it on.")
    }
}

@Composable
private fun OnboardingPoint(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        Box(Modifier.padding(top = 8.dp).size(8.dp).clip(CircleShape).background(Terracotta))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Ink)
    }
}
