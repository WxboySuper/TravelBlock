package com.travelblock.app.ui.screen.home

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.travelblock.app.MainActivity
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.ui.theme.TravelBlockTheme
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    @Ignore("Blocked on physical-device Compose test harness: androidx.test.services setup leaves no Compose hierarchy.")
    fun homeShowsRouteFirstContentAndHandlesDurationTap() {
        var selectedDuration = 25

        composeRule.activityRule.scenario.onActivity { activity ->
            activity.setContent {
                TravelBlockTheme {
                    HomeScreenContent(
                        uiState = HomeUiState(
                            currentAirport = koun,
                            selectedDurationMinutes = selectedDuration,
                            routes = listOf(route),
                            selectedRouteId = route.id,
                        ),
                        onDurationSelected = { selectedDuration = it },
                        onRouteSelected = {},
                        onBookSelectedRoute = {},
                    )
                }
            }
        }

        composeRule.onNodeWithText("TravelBlock").assertIsDisplayed()
        composeRule.onNodeWithText("Available Departures").assertIsDisplayed()
        composeRule.onNodeWithText("KOKC").assertIsDisplayed()

        composeRule.onNodeWithText("60m").performClick()

        assertEquals(60, selectedDuration)
    }

    private val koun = Airport(
        code = "KOUN",
        icao = "KOUN",
        iata = "OUN",
        name = "University of Oklahoma Westheimer Airport",
        city = "Norman",
        stateOrRegion = "Oklahoma",
        country = "US",
        latitude = 35.2456,
        longitude = -97.4721,
        elevationFeet = 1182,
    )

    private val route = HomeRouteCardUiModel(
        id = "KOKC",
        originCode = "KOUN",
        originLatitude = 35.2456,
        originLongitude = -97.4721,
        destinationCode = "KOKC",
        destinationIcao = "KOKC",
        destinationName = "Will Rogers World Airport",
        destinationCityRegion = "Oklahoma City, Oklahoma",
        destinationLatitude = 35.3931,
        destinationLongitude = -97.6007,
        distanceMiles = 13.0,
        durationMinutes = 15,
        distanceLabel = "13 mi",
        focusTimeLabel = "15 min flight",
        estimatedPointsLabel = "+126 pts",
        availabilityLabel = "Short hop",
    )
}
