package com.travelblock.app.ui.screen.book

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.travelblock.app.data.todoist.TodoistRepository
import com.travelblock.app.data.todoist.TodoistTask
import com.travelblock.app.domain.model.TaskRefs
import com.travelblock.app.ui.components.Kicker
import com.travelblock.app.ui.components.PaperCard
import com.travelblock.app.ui.components.SecondaryButton
import com.travelblock.app.ui.components.SoftChip
import com.travelblock.app.ui.components.StampPill
import com.travelblock.app.ui.theme.Cream
import com.travelblock.app.ui.theme.DuskBlue
import com.travelblock.app.ui.theme.Ink
import com.travelblock.app.ui.theme.Paper
import com.travelblock.app.ui.theme.Sage
import com.travelblock.app.ui.theme.Sand
import com.travelblock.app.ui.theme.WarmGray

internal data class BookingManifestActions(
    val objectiveChanged: (String) -> Unit,
    val focusTagSelected: (String) -> Unit,
    val todoistRepository: TodoistRepository?,
    val todoistTaskSelected: (TodoistTask?) -> Unit,
    val openTodoistSettings: () -> Unit,
    val preflightCheckChanged: (String, Boolean) -> Unit,
    val skipToSeats: () -> Unit,
)

private data class PreflightChecks(val cabin: Boolean, val materials: Boolean, val distractions: Boolean)
private data class CheckRow(val key: String, val title: String, val detail: String, val checked: Boolean)

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ManifestCard(uiState: BookingUiState, actions: BookingManifestActions) {
    PaperCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            ManifestHeading(uiState.flightNumber)
            ManifestFocus(uiState, actions)
            SecurityCheckList(
                checks = PreflightChecks(uiState.cabinSecured, uiState.materialsReady, uiState.distractionsStowed),
                onCheckedChanged = actions.preflightCheckChanged,
            )
            SecondaryButton(text = "Skip manifest and choose seat", onClick = actions.skipToSeats)
        }
    }
}

@Composable
private fun ManifestHeading(flightNumber: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Trip manifest", style = MaterialTheme.typography.titleLarge, color = Ink)
            Text("What are you studying on this flight?", style = MaterialTheme.typography.bodyMedium, color = WarmGray)
        }
        StampPill(text = flightNumber)
    }
}

@Composable
private fun ManifestFocus(uiState: BookingUiState, actions: BookingManifestActions) {
    FocusCargoField(uiState.focusObjective, actions.objectiveChanged)
    TodoistTaskPicker(
        repository = actions.todoistRepository,
        selectedIds = TaskRefs.decode(uiState.todoistTaskId, uiState.todoistTaskContent).map { it.id }.toSet(),
        onTaskSelected = actions.todoistTaskSelected,
        onOpenSettings = actions.openTodoistSettings,
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Kicker("Cabin tag")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BookingViewModel.FocusTags.forEach { tag ->
                SoftChip(tag, uiState.focusTag == tag, onClick = { actions.focusTagSelected(tag) })
            }
        }
    }
}

@Composable
private fun SecurityCheckList(checks: PreflightChecks, onCheckedChanged: (String, Boolean) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Kicker("Before we push back")
        listOf(
            CheckRow("cabin", "Desk cleared", "A tidy space makes for a calmer flight", checks.cabin),
            CheckRow("materials", "Materials within reach", "Notes, charger, water", checks.materials),
            CheckRow("distractions", "Phone stowed", "Face down, out of sight", checks.distractions),
        ).forEach { SecurityRow(it, onCheckedChanged) }
    }
}

@Composable
private fun SecurityRow(row: CheckRow, onCheckedChanged: (String, Boolean) -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = Modifier.fillMaxWidth().clip(shape).toggleable(
            value = row.checked,
            role = Role.Checkbox,
            onValueChange = { onCheckedChanged(row.key, it) },
        ),
        color = if (row.checked) Sage.copy(alpha = 0.12f) else Cream,
        shape = shape,
        border = BorderStroke(1.dp, if (row.checked) Sage.copy(alpha = 0.55f) else Sand),
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(26.dp), shape = CircleShape, color = if (row.checked) Sage else Paper, border = BorderStroke(1.5.dp, if (row.checked) Sage else Sand)) {
                Box(contentAlignment = Alignment.Center) {
                    if (row.checked) Icon(Icons.Rounded.Check, contentDescription = null, tint = Paper, modifier = Modifier.size(16.dp))
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(row.title, style = MaterialTheme.typography.titleSmall, color = Ink)
                Text(row.detail, style = MaterialTheme.typography.bodySmall, color = WarmGray)
            }
        }
    }
}

@Composable
private fun FocusCargoField(value: String, onValueChange: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Cream,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Sand),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Kicker("Focus cargo", color = DuskBlue)
                Text("${value.length}/120", style = MaterialTheme.typography.labelSmall, color = WarmGray)
            }
            Box(Modifier.fillMaxWidth().height(76.dp)) {
                if (value.isBlank()) Text("e.g. Finish chapter 4 notes", style = MaterialTheme.typography.bodyLarge, color = WarmGray.copy(alpha = 0.7f))
                BasicTextField(value, onValueChange, modifier = Modifier.fillMaxSize(), textStyle = MaterialTheme.typography.bodyLarge.copy(color = Ink), cursorBrush = SolidColor(DuskBlue))
            }
        }
    }
}

@Composable
internal fun PhaseHeader(eyebrow: String, title: String, detail: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Kicker(eyebrow, color = DuskBlue)
        Text(title, style = MaterialTheme.typography.headlineMedium, color = Ink)
        Text(detail, style = MaterialTheme.typography.bodyMedium, color = WarmGray)
    }
}
