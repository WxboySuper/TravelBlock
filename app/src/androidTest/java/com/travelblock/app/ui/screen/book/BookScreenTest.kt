package com.travelblock.app.ui.screen.book

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.travelblock.app.MainActivity
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.BoardingPass
import com.travelblock.app.domain.model.DraftFlightBooking
import com.travelblock.app.ui.theme.TravelBlockTheme
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class BookScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    @Ignore("Blocked on physical-device Compose test harness: androidx.test.services setup leaves no Compose hierarchy.")
    fun bookScreenShowsDestinationAndSeatSelection() {
        var started: BoardingPass? = null

        composeRule.activityRule.scenario.onActivity { activity ->
            activity.setContent {
                TravelBlockTheme {
                BookScreen(
                    draftFlightBooking = draft,
                    pointsBalance = 20,
                    onCancelBooking = {},
                    onStartFocusFlight = { started = it },
                )
                }
            }
        }

        composeRule.onNodeWithText("Book Flight").assertIsDisplayed()
        composeRule.onNodeWithText("KOKC").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("12F").performClick()
        composeRule.onNodeWithText("Continue to Boarding Pass").performClick()
        composeRule.onNodeWithText("Boarding Pass").assertIsDisplayed()
        composeRule.onNodeWithText("Start Focus Flight").performClick()

        assertTrue(started != null)
    }

    private val draft = DraftFlightBooking(
        origin = Airport(
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
        ),
        destination = Airport(
            code = "KOKC",
            icao = "KOKC",
            iata = "OKC",
            name = "Will Rogers World Airport",
            city = "Oklahoma City",
            stateOrRegion = "Oklahoma",
            country = "US",
            latitude = 35.3931,
            longitude = -97.6007,
            elevationFeet = 1295,
        ),
        durationMinutes = 25,
        distanceMiles = 13.4,
        estimatedRewardPoints = 126,
    )
}
