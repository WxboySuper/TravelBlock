package com.travelblock.app.ui.screen.arrival

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.travelblock.app.data.todoist.TodoistRepository
import com.travelblock.app.data.todoist.TodoistResult
import com.travelblock.app.ui.components.PaperCard
import com.travelblock.app.ui.components.PrimaryButton
import com.travelblock.app.ui.components.SecondaryButton
import com.travelblock.app.ui.theme.Ink
import com.travelblock.app.ui.theme.WarmGray
import kotlinx.coroutines.launch

data class TodoistArrivalTask(val id: String, val content: String)

@Composable
fun TodoistArrivalCard(task: TodoistArrivalTask?, repository: TodoistRepository?) {
    if (task == null || repository == null) return
    TodoistArrivalCardContent(task, repository)
}

@Composable
private fun TodoistArrivalCardContent(task: TodoistArrivalTask, repository: TodoistRepository) {
    var dismissed by remember(task.id) { mutableStateOf(false) }
    var completed by remember(task.id) { mutableStateOf(false) }
    var busy by remember(task.id) { mutableStateOf(false) }
    var error by remember(task.id) { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    if (dismissed) return
    PaperCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("You flew on: ${task.content}", color = Ink, style = MaterialTheme.typography.titleMedium)
            CompletionChoice(CompletionUiState(completed, busy, error), CompletionActions(complete = {
                busy = true
                scope.launch {
                    when (val result = repository.completeTask(task.id)) {
                        is TodoistResult.Success -> { completed = true; error = null }
                        is TodoistResult.Failure -> error = result.message
                    }
                    busy = false
                }
            }, dismiss = { dismissed = true }))
        }
    }
}

@Composable
private fun CompletionChoice(
    state: CompletionUiState,
    actions: CompletionActions,
) {
    if (state.completed) {
        Text("Done in Todoist", color = WarmGray)
        return
    }
    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    if (state.error?.contains("token invalid") == true) Text("Reconnect in Settings.", color = WarmGray)
    PrimaryButton(
        text = if (state.busy) "Marking complete…" else if (state.error != null) "Retry" else "Mark task complete",
        enabled = !state.busy,
        onClick = actions.complete,
    )
    SecondaryButton("Not yet", onClick = actions.dismiss)
}

private data class CompletionUiState(val completed: Boolean, val busy: Boolean, val error: String?)
private data class CompletionActions(val complete: () -> Unit, val dismiss: () -> Unit)
