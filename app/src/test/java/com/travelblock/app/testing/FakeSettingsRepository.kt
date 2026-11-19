package com.travelblock.app.testing

import com.travelblock.app.data.settings.SettingsRepository
import com.travelblock.app.data.settings.UserSettings
import com.travelblock.app.data.settings.AirportCode
import com.travelblock.app.data.settings.ThemePreference
import com.travelblock.app.data.settings.SettingEnabled
import com.travelblock.app.data.settings.FlightDurationSeconds
import com.travelblock.app.data.settings.EquippedItemId
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import kotlin.coroutines.Continuation
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSettingsRepository(initialSettings: UserSettings = UserSettings()) {
    val state = MutableStateFlow(initialSettings)
    var currentAirportCode: String = initialSettings.currentAirportCode
        private set

    val repository: SettingsRepository = Proxy.newProxyInstance(
        SettingsRepository::class.java.classLoader,
        arrayOf(SettingsRepository::class.java),
        InvocationHandler { _, method, arguments ->
            val value = arguments?.firstOrNull()
            when (method.name) {
                "getSettings" -> if (arguments?.firstOrNull() is Continuation<*>) state.value else state
                "setHomeAirportCode" -> state.value = state.value.copy(homeAirportCode = (value as AirportCode).value)
                "setCurrentAirportCode" -> {
                    currentAirportCode = (value as AirportCode).value
                    state.value = state.value.copy(currentAirportCode = currentAirportCode)
                }
                "setSelectedTheme" -> state.value = state.value.copy(selectedTheme = (value as ThemePreference).value)
                "setSoundEnabled" -> state.value = state.value.copy(soundEnabled = (value as SettingEnabled).value)
                "setAnnouncementsEnabled" -> state.value = state.value.copy(announcementsEnabled = (value as SettingEnabled).value)
                "setHapticsEnabled" -> state.value = state.value.copy(hapticsEnabled = (value as SettingEnabled).value)
                "setOnboardingComplete" -> state.value = state.value.copy(onboardingComplete = (value as SettingEnabled).value)
                "setDebugShortFlightsEnabled" -> state.value = state.value.copy(debugShortFlightsEnabled = (value as SettingEnabled).value)
                "setDebugFlightDurationSeconds" -> state.value = state.value.copy(debugFlightDurationSeconds = (value as FlightDurationSeconds).value)
                "setEquippedBoardingPassThemeId" -> state.value = state.value.copy(equippedBoardingPassThemeId = (value as EquippedItemId).value)
                "setEquippedAircraftIconId" -> state.value = state.value.copy(equippedAircraftIconId = (value as EquippedItemId).value)
                "resetToDefaults" -> {
                    currentAirportCode = UserSettings.DefaultAirportCode
                    state.value = UserSettings()
                }
                else -> error("Unexpected settings repository call: ${method.name}")
            }
        },
    ) as SettingsRepository

    suspend fun getSettings(): UserSettings = state.value
}
