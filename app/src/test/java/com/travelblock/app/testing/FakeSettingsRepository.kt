package com.travelblock.app.testing

import com.travelblock.app.data.settings.SettingsRepository
import com.travelblock.app.data.settings.UserSettings
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
                "setHomeAirportCode" -> state.value = state.value.copy(homeAirportCode = value as String)
                "setCurrentAirportCode" -> {
                    currentAirportCode = value as String
                    state.value = state.value.copy(currentAirportCode = currentAirportCode)
                }
                "setSelectedTheme" -> state.value = state.value.copy(selectedTheme = value as String)
                "setSoundEnabled" -> state.value = state.value.copy(soundEnabled = value as Boolean)
                "setAnnouncementsEnabled" -> state.value = state.value.copy(announcementsEnabled = value as Boolean)
                "setHapticsEnabled" -> state.value = state.value.copy(hapticsEnabled = value as Boolean)
                "setOnboardingComplete" -> state.value = state.value.copy(onboardingComplete = value as Boolean)
                "setDebugShortFlightsEnabled" -> state.value = state.value.copy(debugShortFlightsEnabled = value as Boolean)
                "setDebugFlightDurationSeconds" -> state.value = state.value.copy(debugFlightDurationSeconds = value as Int)
                "setEquippedBoardingPassThemeId" -> state.value = state.value.copy(equippedBoardingPassThemeId = value as String)
                "setEquippedAircraftIconId" -> state.value = state.value.copy(equippedAircraftIconId = value as String)
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
