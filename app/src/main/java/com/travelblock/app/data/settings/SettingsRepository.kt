package com.travelblock.app.data.settings

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<UserSettings>
    suspend fun getSettings(): UserSettings
    suspend fun setHomeAirportCode(code: String)
    suspend fun setCurrentAirportCode(code: String)
    suspend fun setSelectedTheme(theme: String)
    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun setAnnouncementsEnabled(enabled: Boolean)
    suspend fun setHapticsEnabled(enabled: Boolean)
    suspend fun setOnboardingComplete(complete: Boolean)
    suspend fun setDebugShortFlightsEnabled(enabled: Boolean)
    suspend fun setDebugFlightDurationSeconds(seconds: Int)
    suspend fun setEquippedBoardingPassThemeId(id: String)
    suspend fun setEquippedAircraftIconId(id: String)
    suspend fun resetToDefaults()
}
