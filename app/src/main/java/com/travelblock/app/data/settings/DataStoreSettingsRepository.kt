package com.travelblock.app.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.travelBlockDataStore by preferencesDataStore(name = "travelblock_settings")

class DataStoreSettingsRepository(
    context: Context,
) : SettingsRepository {
    private val dataStore = context.travelBlockDataStore

    override val settings: Flow<UserSettings> = dataStore.data.map { preferences ->
        val homeAirportCode = preferences[HomeAirportCodeKey] ?: UserSettings.DefaultAirportCode
        UserSettings(
            homeAirportCode = homeAirportCode,
            currentAirportCode = preferences[CurrentAirportCodeKey] ?: homeAirportCode,
            selectedTheme = preferences[SelectedThemeKey] ?: "system",
            soundEnabled = preferences[SoundEnabledKey] ?: true,
            hapticsEnabled = preferences[HapticsEnabledKey] ?: true,
            onboardingComplete = preferences[OnboardingCompleteKey] ?: true,
            equippedBoardingPassThemeId = preferences[EquippedBoardingPassThemeIdKey] ?: "",
            equippedAircraftIconId = preferences[EquippedAircraftIconIdKey] ?: "",
        )
    }

    override suspend fun getSettings(): UserSettings = settings.first()

    override suspend fun setHomeAirportCode(code: String) {
        val normalized = code.trim().uppercase().ifBlank { UserSettings.DefaultAirportCode }
        dataStore.edit { preferences ->
            preferences[HomeAirportCodeKey] = normalized
            preferences[CurrentAirportCodeKey] = preferences[CurrentAirportCodeKey] ?: normalized
        }
    }

    override suspend fun setCurrentAirportCode(code: String) {
        val normalized = code.trim().uppercase().ifBlank { UserSettings.DefaultAirportCode }
        dataStore.edit { preferences ->
            preferences[CurrentAirportCodeKey] = normalized
        }
    }

    override suspend fun setSelectedTheme(theme: String) {
        val normalized = when (theme.trim().lowercase()) {
            "light", "dark" -> theme.trim().lowercase()
            else -> "system"
        }
        dataStore.edit { preferences ->
            preferences[SelectedThemeKey] = normalized
        }
    }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SoundEnabledKey] = enabled
        }
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[HapticsEnabledKey] = enabled
        }
    }

    override suspend fun setEquippedBoardingPassThemeId(id: String) {
        dataStore.edit { preferences ->
            preferences[EquippedBoardingPassThemeIdKey] = id
        }
    }

    override suspend fun setEquippedAircraftIconId(id: String) {
        dataStore.edit { preferences ->
            preferences[EquippedAircraftIconIdKey] = id
        }
    }

    override suspend fun resetToDefaults() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private companion object {
        val HomeAirportCodeKey = stringPreferencesKey("homeAirportCode")
        val CurrentAirportCodeKey = stringPreferencesKey("currentAirportCode")
        val SelectedThemeKey = stringPreferencesKey("selectedTheme")
        val SoundEnabledKey = booleanPreferencesKey("soundEnabled")
        val HapticsEnabledKey = booleanPreferencesKey("hapticsEnabled")
        val OnboardingCompleteKey = booleanPreferencesKey("onboardingComplete")
        val EquippedBoardingPassThemeIdKey = stringPreferencesKey("equippedBoardingPassThemeId")
        val EquippedAircraftIconIdKey = stringPreferencesKey("equippedAircraftIconId")
    }
}
