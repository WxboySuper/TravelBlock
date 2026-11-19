package com.travelblock.app.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelblock.app.TravelBlockContainer
import com.travelblock.app.data.todoist.TodoistResult
import com.travelblock.app.data.settings.UserSettings
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.ui.components.SectionHeader
import com.travelblock.app.ui.components.StatusChip
import com.travelblock.app.ui.components.StatusTone
import com.travelblock.app.ui.theme.CabinAmber
import com.travelblock.app.ui.theme.MutedText
import com.travelblock.app.ui.theme.RunwayLine
import com.travelblock.app.ui.theme.SoftSky
import com.travelblock.app.update.UpdateUiState
import com.travelblock.app.BuildConfig
import com.travelblock.app.ui.components.PrimaryButton
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class AirportPickerActions(
    val open: (AirportPickerMode) -> Unit,
    val dismiss: () -> Unit,
    val searchChanged: (String) -> Unit,
    val selected: (Airport) -> Unit,
)

data class HomeAirportActions(val confirm: (Boolean) -> Unit, val dismiss: () -> Unit)
data class AirportActions(val picker: AirportPickerActions, val home: HomeAirportActions)
data class SoundSettingsActions(
    val themeSelected: (String) -> Unit,
    val soundChanged: (Boolean) -> Unit,
    val announcementsChanged: (Boolean) -> Unit,
    val hapticsChanged: (Boolean) -> Unit,
)
data class DebugSettingsActions(val shortFlightsChanged: (Boolean) -> Unit, val durationSelected: (Int) -> Unit)
data class PreferenceActions(val sound: SoundSettingsActions, val debug: DebugSettingsActions)
data class ResetActions(val show: () -> Unit, val dismiss: () -> Unit, val reset: () -> Unit)
data class SettingsActions(val airports: AirportActions, val preferences: PreferenceActions, val reset: ResetActions)

@Composable
fun SettingsScreen(container: TravelBlockContainer) {
    val updateState by container.updateManager.state.collectAsState()
    val lastCheck by container.updateManager.lastCheckMillis.collectAsState()
    val scope = rememberCoroutineScope()
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(
            airportRepository = container.airportRepository,
            settingsRepository = container.settingsRepository,
            resetLocalData = container::resetLocalData,
        ),
    )

    SettingsScreenContent(
        uiState = viewModel.uiState,
        updateState = updateState,
        lastCheckMillis = lastCheck,
        onCheckUpdates = { scope.launch { container.updateManager.check(manual = true) } },
        todoistRepository = container.todoistRepository,
        actions = SettingsActions(
            airports = AirportActions(
                picker = AirportPickerActions(viewModel::openAirportPicker, viewModel::dismissAirportPicker, viewModel::onAirportSearchChanged, viewModel::selectAirport),
                home = HomeAirportActions(viewModel::confirmHomeAirportChange, viewModel::dismissHomeAirportConfirmation),
            ),
            preferences = PreferenceActions(
                sound = SoundSettingsActions(viewModel::setSelectedTheme, viewModel::setSoundEnabled, viewModel::setAnnouncementsEnabled, viewModel::setHapticsEnabled),
                debug = DebugSettingsActions(viewModel::setDebugShortFlightsEnabled, viewModel::setDebugFlightDurationSeconds),
            ),
            reset = ResetActions(viewModel::showResetConfirmation, viewModel::dismissResetConfirmation, viewModel::resetAllLocalData),
        ),
    )
}

@Composable
fun SettingsScreenContent(
    uiState: SettingsUiState,
    actions: SettingsActions,
    updateState: UpdateUiState = UpdateUiState.Idle,
    lastCheckMillis: Long = 0L,
    onCheckUpdates: () -> Unit = {},
    todoistRepository: com.travelblock.app.data.todoist.TodoistRepository? = null,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            SectionHeader(
                title = "Cabin Controls",
                subtitle = "Manage airports, comfort settings, and local app data.",
            )
        }
        item {
            PilotProfileCard()
        }
        item {
            AirportSettingsCard(
                homeAirport = uiState.homeAirport,
                currentAirport = uiState.currentAirport,
                onChangeHome = { actions.airports.picker.open(AirportPickerMode.Home) },
                onChangeCurrent = { actions.airports.picker.open(AirportPickerMode.Current) },
            )
        }
        item {
            PreferenceCard(uiState.settings, actions.preferences)
        }
        item { PaletteCard() }
        item { AppUpdatesCard(updateState, lastCheckMillis, onCheckUpdates) }
        if (todoistRepository != null) item { TodoistSettingsCard(todoistRepository) }
        item {
            ResetDataCard(onShowResetConfirmation = actions.reset.show)
        }
        item {
            AboutCard()
            uiState.message?.let { MessagePill(it) }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    SettingsOverlays(uiState, actions)
}

@Composable
private fun TodoistSettingsCard(repository: com.travelblock.app.data.todoist.TodoistRepository) {
    val connected by repository.isConnected.collectAsState(initial = false)
    val scope = rememberCoroutineScope()
    var token by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    SettingsCard {
        Text("Todoist", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(if (connected) "Connected" else "Not connected", color = MutedText)
        if (!connected) {
            OutlinedTextField(
                value = token,
                onValueChange = { token = it },
                label = { Text("Personal API token") },
                singleLine = true,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            Text("Find it in Todoist, Settings → Integrations → Developer.", color = MutedText, style = MaterialTheme.typography.bodySmall)
            Button(onClick = {
                busy = true
                message = null
                scope.launch {
                    when (val result = repository.connect(token)) {
                        is TodoistResult.Success -> { message = "Connected"; token = "" }
                        is TodoistResult.Failure -> message = result.message
                    }
                    busy = false
                }
            }, enabled = token.isNotBlank() && !busy) { Text(if (busy) "Checking…" else "Connect") }
        } else {
            OutlinedButton(onClick = { scope.launch { repository.disconnect(); message = "Disconnected" } }) { Text("Disconnect") }
        }
        message?.let { Text(it, color = if (it == "Connected") CabinAmber else MaterialTheme.colorScheme.error) }
    }
}

@Composable
private fun AppUpdatesCard(state: UpdateUiState, lastCheckMillis: Long, onCheck: () -> Unit) {
    SettingsCard {
        Text("App updates", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Version ${BuildConfig.VERSION_NAME} (build ${BuildConfig.BUILD_NUMBER})", color = MutedText)
        val checkedAt = lastCheckMillis.takeIf { it > 0 }?.let {
            DateTimeFormatter.ofPattern("MMM d, h:mm a").withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(it))
        } ?: "Not checked yet"
        Text("Last checked: $checkedAt", style = MaterialTheme.typography.bodySmall, color = MutedText)
        val result = when (state) {
            is UpdateUiState.Available -> "Update available: build ${state.info.build}"
            UpdateUiState.UpToDate -> "You're up to date"
            UpdateUiState.Checking -> "Checking for updates..."
            is UpdateUiState.Error -> state.message
            else -> ""
        }
        if (result.isNotBlank()) Text(result, style = MaterialTheme.typography.bodyMedium)
        PrimaryButton("Check for updates", onCheck, enabled = state != UpdateUiState.Checking)
    }
}

@Composable
private fun SettingsOverlays(uiState: SettingsUiState, actions: SettingsActions) {
    AirportPickerOverlay(uiState, actions.airports.picker.searchChanged, actions.airports.picker.selected, actions.airports.picker.dismiss)
    HomeAirportConfirmationOverlay(uiState.pendingHomeAirport, actions.airports.home.confirm, actions.airports.home.dismiss)
    ResetConfirmationOverlay(uiState.showResetConfirmation, actions.reset.reset, actions.reset.dismiss)
}

@Composable
private fun AirportPickerOverlay(
    uiState: SettingsUiState,
    onSearchChanged: (String) -> Unit,
    onAirportSelected: (Airport) -> Unit,
    onDismiss: () -> Unit,
) {
    uiState.pickerMode?.let { mode ->
        AirportPickerDialog(
            mode = mode,
            query = uiState.airportSearchQuery,
            results = uiState.airportSearchResults,
            onQueryChanged = onSearchChanged,
            onAirportSelected = onAirportSelected,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun HomeAirportConfirmationOverlay(
    airport: Airport?,
    onConfirm: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    airport ?: return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set ${airport.code} as home?") },
        text = { Text("You can keep your current journey where it is, or move your current airport to the new home airport too.") },
        confirmButton = { Button(onClick = { onConfirm(true) }) { Text("Move Current Too") } },
        dismissButton = { TextButton(onClick = { onConfirm(false) }) { Text("Home Only") } },
    )
}

@Composable
private fun ResetConfirmationOverlay(
    visible: Boolean,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset local data?") },
        text = { Text("This clears saved flights, points, unlocks, and preferences, then restores the default KOUN setup. This cannot be undone.") },
        confirmButton = { Button(onClick = onReset) { Text("Reset Data") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun PilotProfileCard() {
    SettingsCard {
        Text("Pilot profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Captain Alex", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Profile details and callsign editing will come after the core flight loop is hardened.", color = MutedText)
    }
}

@Composable
private fun AirportSettingsCard(
    homeAirport: Airport?,
    currentAirport: Airport?,
    onChangeHome: () -> Unit,
    onChangeCurrent: () -> Unit,
) {
    SettingsCard {
        Text("Airports", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        AirportPreferenceRow(
            label = "Home airport",
            airport = homeAirport,
            actionLabel = "Change Home",
            onClick = onChangeHome,
        )
        AirportPreferenceRow(
            label = "Current airport",
            airport = currentAirport,
            actionLabel = "Move Current",
            onClick = onChangeCurrent,
        )
        Text(
            "Home is your base. Current airport controls the routes shown on Home.",
            style = MaterialTheme.typography.bodySmall,
            color = MutedText,
        )
    }
}

@Composable
private fun AirportPreferenceRow(
    label: String,
    airport: Airport?,
    actionLabel: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MutedText)
            Text(airport?.code ?: "KOUN", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                airport?.let { "${it.name} - ${it.city}" } ?: "University of Oklahoma Westheimer Airport",
                style = MaterialTheme.typography.bodySmall,
                color = MutedText,
            )
        }
        OutlinedButton(onClick = onClick, shape = RoundedCornerShape(12.dp)) {
            Text(actionLabel)
        }
    }
}

@Composable
private fun PreferenceCard(
    settings: UserSettings,
    actions: PreferenceActions,
) {
    SettingsCard {
        Text("Comfort", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        ToggleRow(
            title = "Sound",
            description = "Controls future cabin and flight event sounds.",
            checked = settings.soundEnabled,
            onCheckedChange = actions.sound.soundChanged,
        )
        ToggleRow(
            title = "PA announcements",
            description = "Speak boarding and flight updates.",
            checked = settings.announcementsEnabled,
            onCheckedChange = actions.sound.announcementsChanged,
        )
        ToggleRow(
            title = "Haptics",
            description = "Controls future selection and flight-event vibration cues.",
            checked = settings.hapticsEnabled,
            onCheckedChange = actions.sound.hapticsChanged,
        )
        if (com.travelblock.app.BuildConfig.DEBUG) {
            Text("Developer tools", style = MaterialTheme.typography.labelLarge, color = MutedText)
            ToggleRow(
                title = "Short test flights",
                description = "Use a 10 to 30 second arrival timer to test flight outcomes.",
                checked = settings.debugShortFlightsEnabled,
                onCheckedChange = actions.debug.shortFlightsChanged,
            )
            if (settings.debugShortFlightsEnabled) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(10, 15, 30).forEach { seconds ->
                        FilterChip(
                            selected = settings.debugFlightDurationSeconds == seconds,
                            onClick = { actions.debug.durationSelected(seconds) },
                            label = { Text("${seconds}s") },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MutedText)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ResetDataCard(onShowResetConfirmation: () -> Unit) {
    SettingsCard {
        Text("Reset local data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            "Clears local flights, points, unlocks, and preferences. Use this when testing a fresh TravelBlock setup.",
            color = MutedText,
        )
        OutlinedButton(onClick = onShowResetConfirmation, shape = RoundedCornerShape(12.dp)) {
            Text("Reset Local Data")
        }
    }
}

@Composable
private fun AboutCard() {
    SettingsCard {
        Text("About TravelBlock", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Native Android focus flights powered by local airport data, Room, DataStore, and Jetpack Compose.", color = MutedText)
        Text("No accounts, cloud sync, real-money purchases, or flight APIs in v1.", style = MaterialTheme.typography.bodySmall, color = MutedText)
    }
}

@Composable
private fun AirportPickerDialog(
    mode: AirportPickerMode,
    query: String,
    results: List<AirportPickerItemUiModel>,
    onQueryChanged: (String) -> Unit,
    onAirportSelected: (Airport) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (mode == AirportPickerMode.Home) "Choose Home Airport" else "Choose Current Airport") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChanged,
                    label = { Text("Search code, city, or airport") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                LazyColumn(
                    modifier = Modifier.height(360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(results, key = { it.code }) { item ->
                        AirportResultCard(
                            item = item,
                            onClick = { onAirportSelected(item.airport) },
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun AirportResultCard(
    item: AirportPickerItemUiModel,
    onClick: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, RunwayLine),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusChip(text = item.code, tone = StatusTone.Info)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(item.location, style = MaterialTheme.typography.bodySmall, color = MutedText)
                item.distanceLabel?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = CabinAmber)
                }
            }
        }
    }
}

@Composable
private fun MessagePill(message: String) {
    Surface(
        color = SoftSky,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.padding(top = 10.dp),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}
