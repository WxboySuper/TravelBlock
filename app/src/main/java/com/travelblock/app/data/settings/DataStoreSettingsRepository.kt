package com.travelblock.app.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.travelBlockDataStore by preferencesDataStore(name = "travelblock_settings")

private data class AudioPreferences(
    val soundEnabled: Boolean,
    val announcementsEnabled: Boolean,
    val hapticsEnabled: Boolean,
)

class DataStoreSettingsRepository(
    context: Context,
) : SettingsRepository {
    private val dataStore = context.travelBlockDataStore

    override val settings: Flow<UserSettings> = dataStore.data.map(::settingsFrom)

    override suspend fun getSettings(): UserSettings = settings.first()

    override suspend fun setHomeAirportCode(code: AirportCode) {
        val normalized = code.value.trim().uppercase().ifBlank { UserSettings.DefaultAirportCode }
        dataStore.edit { preferences ->
            preferences[HomeAirportCodeKey] = normalized
            preferences[CurrentAirportCodeKey] = preferences[CurrentAirportCodeKey] ?: normalized
        }
    }

    override suspend fun setCurrentAirportCode(code: AirportCode) {
        val normalized = code.value.trim().uppercase().ifBlank { UserSettings.DefaultAirportCode }
        dataStore.edit { preferences ->
            preferences[CurrentAirportCodeKey] = normalized
        }
    }

    override suspend fun setSelectedTheme(theme: ThemePreference) {
        val normalized = when (theme.value.trim().lowercase()) {
            "light", "dark" -> theme.value.trim().lowercase()
            else -> "system"
        }
        dataStore.edit { preferences ->
            preferences[SelectedThemeKey] = normalized
        }
    }

    override suspend fun setSoundEnabled(enabled: SettingEnabled) {
        dataStore.edit { preferences ->
            preferences[SoundEnabledKey] = enabled.value
        }
    }

    override suspend fun setAnnouncementsEnabled(enabled: SettingEnabled) {
        dataStore.edit { preferences -> preferences[AnnouncementsEnabledKey] = enabled.value }
    }

    override suspend fun setHapticsEnabled(enabled: SettingEnabled) {
        dataStore.edit { preferences ->
            preferences[HapticsEnabledKey] = enabled.value
        }
    }

    override suspend fun setOnboardingComplete(complete: SettingEnabled) {
        dataStore.edit { preferences -> preferences[OnboardingCompleteKey] = complete.value }
    }

    override suspend fun setDebugShortFlightsEnabled(enabled: SettingEnabled) {
        dataStore.edit { preferences -> preferences[DebugShortFlightsEnabledKey] = enabled.value }
    }

    override suspend fun setDebugFlightDurationSeconds(seconds: FlightDurationSeconds) {
        dataStore.edit { preferences -> preferences[DebugFlightDurationSecondsKey] = seconds.value.coerceIn(10, 30) }
    }

    override suspend fun setEquippedBoardingPassThemeId(id: EquippedItemId) {
        dataStore.edit { preferences ->
            preferences[EquippedBoardingPassThemeIdKey] = id.value
        }
    }

    override suspend fun setEquippedAircraftIconId(id: EquippedItemId) {
        dataStore.edit { preferences ->
            preferences[EquippedAircraftIconIdKey] = id.value
        }
    }

    override suspend fun resetToDefaults() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private fun settingsFrom(preferences: Preferences): UserSettings {
        val homeAirportCode = preferences.homeAirportCode()
        val audioPreferences = preferences.audioPreferences()
        return UserSettings(
            homeAirportCode = homeAirportCode,
            currentAirportCode = preferences.currentAirportCode(homeAirportCode),
            selectedTheme = preferences[SelectedThemeKey] ?: "system",
            soundEnabled = audioPreferences.soundEnabled,
            announcementsEnabled = audioPreferences.announcementsEnabled,
            hapticsEnabled = audioPreferences.hapticsEnabled,
            onboardingComplete = preferences[OnboardingCompleteKey] ?: false,
            debugShortFlightsEnabled = preferences[DebugShortFlightsEnabledKey] ?: false,
            debugFlightDurationSeconds = preferences[DebugFlightDurationSecondsKey] ?: 15,
            equippedBoardingPassThemeId = preferences[EquippedBoardingPassThemeIdKey] ?: "",
            equippedAircraftIconId = preferences[EquippedAircraftIconIdKey] ?: "",
        )
    }

    private fun Preferences.homeAirportCode() = this[HomeAirportCodeKey] ?: UserSettings.DefaultAirportCode

    private fun Preferences.currentAirportCode(homeAirportCode: String) = this[CurrentAirportCodeKey] ?: homeAirportCode

    private fun Preferences.audioPreferences() = AudioPreferences(
        soundEnabled = this[SoundEnabledKey] ?: true,
        announcementsEnabled = this[AnnouncementsEnabledKey] ?: true,
        hapticsEnabled = this[HapticsEnabledKey] ?: true,
    )

    private companion object {
        val HomeAirportCodeKey = stringPreferencesKey("homeAirportCode")
        val CurrentAirportCodeKey = stringPreferencesKey("currentAirportCode")
        val SelectedThemeKey = stringPreferencesKey("selectedTheme")
        val SoundEnabledKey = booleanPreferencesKey("soundEnabled")
        val AnnouncementsEnabledKey = booleanPreferencesKey("announcementsEnabled")
        val HapticsEnabledKey = booleanPreferencesKey("hapticsEnabled")
        val OnboardingCompleteKey = booleanPreferencesKey("onboardingComplete")
        val DebugShortFlightsEnabledKey = booleanPreferencesKey("debugShortFlightsEnabled")
        val DebugFlightDurationSecondsKey = intPreferencesKey("debugFlightDurationSeconds")
        val EquippedBoardingPassThemeIdKey = stringPreferencesKey("equippedBoardingPassThemeId")
        val EquippedAircraftIconIdKey = stringPreferencesKey("equippedAircraftIconId")
    }
}
