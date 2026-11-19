package com.travelblock.app.data.settings

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<UserSettings>
    suspend fun getSettings(): UserSettings
    suspend fun setHomeAirportCode(code: AirportCode)
    suspend fun setCurrentAirportCode(code: AirportCode)
    suspend fun setSelectedTheme(theme: ThemePreference)
    suspend fun setSoundEnabled(enabled: SettingEnabled)
    suspend fun setAnnouncementsEnabled(enabled: SettingEnabled)
    suspend fun setHapticsEnabled(enabled: SettingEnabled)
    suspend fun setKeepScreenAwake(enabled: SettingEnabled)
    suspend fun setOnboardingComplete(complete: SettingEnabled)
    suspend fun setDebugShortFlightsEnabled(enabled: SettingEnabled)
    suspend fun setDebugFlightDurationSeconds(seconds: FlightDurationSeconds)
    suspend fun setEquippedBoardingPassThemeId(id: EquippedItemId)
    suspend fun setEquippedAircraftIconId(id: EquippedItemId)
    suspend fun resetToDefaults()
}
