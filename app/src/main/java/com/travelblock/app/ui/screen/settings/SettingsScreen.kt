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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelblock.app.TravelBlockContainer
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.ui.components.SectionHeader
import com.travelblock.app.ui.components.StatusChip
import com.travelblock.app.ui.components.StatusTone
import com.travelblock.app.ui.theme.CabinAmber
import com.travelblock.app.ui.theme.MutedText
import com.travelblock.app.ui.theme.RunwayLine
import com.travelblock.app.ui.theme.SoftSky

@Composable
fun SettingsScreen(container: TravelBlockContainer) {
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(
            airportRepository = container.airportRepository,
            settingsRepository = container.settingsRepository,
            resetLocalData = container::resetLocalData,
        ),
    )

    SettingsScreenContent(
        uiState = viewModel.uiState,
        onOpenAirportPicker = viewModel::openAirportPicker,
        onDismissAirportPicker = viewModel::dismissAirportPicker,
        onAirportSearchChanged = viewModel::onAirportSearchChanged,
        onAirportSelected = viewModel::selectAirport,
        onConfirmHomeAirportChange = viewModel::confirmHomeAirportChange,
        onDismissHomeAirportConfirmation = viewModel::dismissHomeAirportConfirmation,
        onThemeSelected = viewModel::setSelectedTheme,
        onSoundEnabledChanged = viewModel::setSoundEnabled,
        onHapticsEnabledChanged = viewModel::setHapticsEnabled,
        onShowResetConfirmation = viewModel::showResetConfirmation,
        onDismissResetConfirmation = viewModel::dismissResetConfirmation,
        onResetLocalData = viewModel::resetAllLocalData,
    )
}

@Composable
fun SettingsScreenContent(
    uiState: SettingsUiState,
    onOpenAirportPicker: (AirportPickerMode) -> Unit,
    onDismissAirportPicker: () -> Unit,
    onAirportSearchChanged: (String) -> Unit,
    onAirportSelected: (Airport) -> Unit,
    onConfirmHomeAirportChange: (Boolean) -> Unit,
    onDismissHomeAirportConfirmation: () -> Unit,
    onThemeSelected: (String) -> Unit,
    onSoundEnabledChanged: (Boolean) -> Unit,
    onHapticsEnabledChanged: (Boolean) -> Unit,
    onShowResetConfirmation: () -> Unit,
    onDismissResetConfirmation: () -> Unit,
    onResetLocalData: () -> Unit,
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
                onChangeHome = { onOpenAirportPicker(AirportPickerMode.Home) },
                onChangeCurrent = { onOpenAirportPicker(AirportPickerMode.Current) },
            )
        }
        item {
            PreferenceCard(
                selectedTheme = uiState.settings.selectedTheme,
                soundEnabled = uiState.settings.soundEnabled,
                hapticsEnabled = uiState.settings.hapticsEnabled,
                onThemeSelected = onThemeSelected,
                onSoundEnabledChanged = onSoundEnabledChanged,
                onHapticsEnabledChanged = onHapticsEnabledChanged,
            )
        }
        item {
            ResetDataCard(onShowResetConfirmation = onShowResetConfirmation)
        }
        item {
            AboutCard()
            uiState.message?.let { MessagePill(it) }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    uiState.pickerMode?.let { mode ->
        AirportPickerDialog(
            mode = mode,
            query = uiState.airportSearchQuery,
            results = uiState.airportSearchResults,
            onQueryChanged = onAirportSearchChanged,
            onAirportSelected = onAirportSelected,
            onDismiss = onDismissAirportPicker,
        )
    }

    uiState.pendingHomeAirport?.let { airport ->
        AlertDialog(
            onDismissRequest = onDismissHomeAirportConfirmation,
            title = { Text("Set ${airport.code} as home?") },
            text = {
                Text(
                    "You can keep your current journey where it is, or move your current airport to the new home airport too.",
                )
            },
            confirmButton = {
                Button(onClick = { onConfirmHomeAirportChange(true) }) {
                    Text("Move Current Too")
                }
            },
            dismissButton = {
                TextButton(onClick = { onConfirmHomeAirportChange(false) }) {
                    Text("Home Only")
                }
            },
        )
    }

    if (uiState.showResetConfirmation) {
        AlertDialog(
            onDismissRequest = onDismissResetConfirmation,
            title = { Text("Reset local data?") },
            text = {
                Text(
                    "This clears saved flights, points, unlocks, and preferences, then restores the default KOUN setup. This cannot be undone.",
                )
            },
            confirmButton = {
                Button(onClick = onResetLocalData) {
                    Text("Reset Data")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissResetConfirmation) {
                    Text("Cancel")
                }
            },
        )
    }
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
    selectedTheme: String,
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
    onThemeSelected: (String) -> Unit,
    onSoundEnabledChanged: (Boolean) -> Unit,
    onHapticsEnabledChanged: (Boolean) -> Unit,
) {
    SettingsCard {
        Text("Comfort", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Theme", style = MaterialTheme.typography.labelLarge, color = MutedText)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeChip("System", "system", selectedTheme, onThemeSelected)
            ThemeChip("Light", "light", selectedTheme, onThemeSelected)
            ThemeChip("Dark", "dark", selectedTheme, onThemeSelected)
        }
        ToggleRow(
            title = "Sound",
            description = "Controls future cabin and flight event sounds.",
            checked = soundEnabled,
            onCheckedChange = onSoundEnabledChanged,
        )
        ToggleRow(
            title = "Haptics",
            description = "Controls future selection and flight-event vibration cues.",
            checked = hapticsEnabled,
            onCheckedChange = onHapticsEnabledChanged,
        )
    }
}

@Composable
private fun ThemeChip(
    label: String,
    value: String,
    selectedTheme: String,
    onThemeSelected: (String) -> Unit,
) {
    FilterChip(
        selected = selectedTheme == value,
        onClick = { onThemeSelected(value) },
        label = { Text(label) },
    )
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
