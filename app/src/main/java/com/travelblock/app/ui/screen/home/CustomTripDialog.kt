package com.travelblock.app.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.travelblock.app.domain.engine.TripTime
import com.travelblock.app.ui.screen.book.formatMinutes
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** A trip length the traveller typed in, or worked out from when they want to land. */
data class CustomTrip(val minutes: Int, val label: String, val landBy: Boolean = false)

/** Ask for a number of minutes, or the time to land by; either becomes the focus time the planner fits. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTripDialog(onDismiss: () -> Unit, onConfirm: (CustomTrip) -> Unit) {
    var byEndTime by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("150") }
    val now = remember { LocalTime.now() }
    val clock = rememberTimePickerState(initialHour = (now.hour + 3) % 24, initialMinute = now.minute, is24Hour = false)
    val end = LocalTime.of(clock.hour, clock.minute)
    val minutes = if (byEndTime) TripTime.focusForElapsed(TripTime.minutesUntil(end, now)) else text.toIntOrNull() ?: 0
    val valid = minutes in TripTime.MIN_FOCUS_MINUTES..TripTime.MAX_FOCUS_MINUTES
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom trip") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(!byEndTime, { byEndTime = false }, label = { Text("Minutes") })
                    FilterChip(byEndTime, { byEndTime = true }, label = { Text("Land by") })
                }
                if (byEndTime) {
                    TimeInput(clock)
                } else {
                    OutlinedTextField(
                        text,
                        { text = it.filter(Char::isDigit).take(3) },
                        label = { Text("Minutes of focus") },
                        singleLine = true,
                    )
                }
                Text(summary(valid, minutes, byEndTime), style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(enabled = valid, onClick = { onConfirm(CustomTrip(minutes, label(byEndTime, end, minutes), byEndTime)) }) { Text("Find trips") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

private fun summary(valid: Boolean, minutes: Int, byEndTime: Boolean): String = when {
    valid && byEndTime -> "That leaves ${formatMinutes(minutes)} of focus, breaks and boarding included."
    valid -> "${formatMinutes(minutes)} of focus."
    else -> "Choose between ${TripTime.MIN_FOCUS_MINUTES} and ${TripTime.MAX_FOCUS_MINUTES} minutes."
}

private fun label(byEndTime: Boolean, end: LocalTime, minutes: Int): String =
    if (byEndTime) "By ${end.format(DateTimeFormatter.ofPattern("h:mm a"))}" else formatMinutes(minutes)
