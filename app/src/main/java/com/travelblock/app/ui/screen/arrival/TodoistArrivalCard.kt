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
fun TodoistArrivalCard(tasks: List<TodoistArrivalTask>, repository: TodoistRepository?) {
    if (tasks.isEmpty() || repository == null) return
    TodoistArrivalCardContent(tasks, repository)
}

@Composable
private fun TodoistArrivalCardContent(tasks: List<TodoistArrivalTask>, repository: TodoistRepository) {
    val key = tasks.joinToString(",") { it.id }
    var dismissed by remember(key) { mutableStateOf(false) }
    var completed by remember(key) { mutableStateOf(false) }
    var busy by remember(key) { mutableStateOf(false) }
    var error by remember(key) { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    if (dismissed) return
    PaperCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(if (tasks.size == 1) "You flew on: ${tasks.single().content}" else "You flew on ${tasks.size} tasks", color = Ink, style = MaterialTheme.typography.titleMedium)
            if (tasks.size > 1) tasks.forEach { Text(it.content, color = WarmGray, style = MaterialTheme.typography.bodyMedium) }
            CompletionChoice(CompletionUiState(completed, busy, error), CompletionActions(complete = {
                busy = true
                scope.launch {
                    val failure = tasks.firstNotNullOfOrNull { task -> (repository.completeTask(task.id) as? TodoistResult.Failure)?.message }
                    completed = failure == null
                    error = failure
                    busy = false
                }
            }, dismiss = { dismissed = true }, plural = tasks.size > 1))
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
private data class CompletionActions(val complete: () -> Unit, val dismiss: () -> Unit, val plural: Boolean = false)
