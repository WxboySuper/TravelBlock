package com.travelblock.app.ui.util

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * Lightweight haptic cues for travel-ritual moments. Safe on devices without vibrators.
 */
class TravelBlockHaptics(
    private val view: View,
    private val enabled: Boolean,
) {
    fun seatSelected() = perform(HapticFeedbackConstants.KEYBOARD_TAP)

    fun boardingPassIssued() = perform(HapticFeedbackConstants.CONTEXT_CLICK)

    fun gateCleared() = perform(HapticFeedbackConstants.CONFIRM)

    fun focusFlightStarted() = perform(HapticFeedbackConstants.LONG_PRESS)

    private fun perform(feedbackConstant: Int) {
        if (!enabled) return
        try {
            view.performHapticFeedback(feedbackConstant)
        } catch (_: Exception) {
            // Ignore unsupported or blocked haptics.
        }
    }
}

@Composable
fun rememberTravelBlockHaptics(enabled: Boolean = true): TravelBlockHaptics {
    val view = LocalView.current
    return remember(view, enabled) { TravelBlockHaptics(view, enabled) }
}
