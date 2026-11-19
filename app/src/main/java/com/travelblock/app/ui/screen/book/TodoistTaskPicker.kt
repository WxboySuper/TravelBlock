package com.travelblock.app.ui.screen.book

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.travelblock.app.data.todoist.TodoistRepository
import com.travelblock.app.data.todoist.TodoistResult
import com.travelblock.app.data.todoist.TodoistTask
import com.travelblock.app.ui.components.PaperCard
import com.travelblock.app.ui.components.SecondaryButton
import com.travelblock.app.ui.theme.Ink
import com.travelblock.app.ui.theme.Marigold
import com.travelblock.app.ui.theme.Sage
import com.travelblock.app.ui.theme.WarmGray
import kotlinx.coroutines.launch

@Composable
fun TodoistTaskPicker(
    repository: TodoistRepository?,
    selectedIds: Set<String>,
    onTaskSelected: (TodoistTask?) -> Unit,
    onOpenSettings: () -> Unit,
) {
    repository?.let { ConnectedTodoistTaskPicker(it, selectedIds, onTaskSelected, onOpenSettings) }
}

@Composable
private fun ConnectedTodoistTaskPicker(
    repository: TodoistRepository,
    selectedIds: Set<String>,
    onTaskSelected: (TodoistTask?) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val connected by repository.isConnected.collectAsState(initial = false)
    PaperCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Focus task", style = MaterialTheme.typography.titleMedium, color = Ink)
            if (!connected) {
                TodoistDisconnectedRow(onOpenSettings)
            } else {
                ConnectedTaskList(repository, selectedIds, onTaskSelected)
            }
        }
    }
}

@Composable
private fun TodoistDisconnectedRow(onOpenSettings: () -> Unit) {
    Text("Todoist isn't connected yet.", color = WarmGray)
    SecondaryButton("Connect Todoist in Settings", onClick = onOpenSettings)
}

@Composable
private fun ConnectedTaskList(
    repository: TodoistRepository,
    selectedIds: Set<String>,
    onTaskSelected: (TodoistTask?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var tasks by remember { mutableStateOf(emptyList<TodoistTask>()) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    Text(selectedSummary(tasks, selectedIds), color = WarmGray)
    SecondaryButton(
        if (expanded) "Close task list" else "Fly on a Todoist task",
        onClick = {
            expanded = !expanded
            if (expanded) scope.launch {
                when (val result = repository.loadTasks()) {
                    is TodoistResult.Success -> tasks = result.value
                    is TodoistResult.Failure -> message = result.message
                }
            }
        },
    )
    if (expanded) {
        TaskSearchResults(
            state = TaskSearchState(tasks, query, selectedIds, message),
            actions = TaskSearchActions({ query = it }, onTaskSelected),
        )
    }
}

/** What is picked so far: the titles, or a count once several are chosen, so the card stays short. */
private fun selectedSummary(tasks: List<TodoistTask>, selectedIds: Set<String>): String {
    val titles = tasks.filter { it.id in selectedIds }.map { it.content }
    return when {
        selectedIds.isEmpty() -> "No task selected. Tap more than one to fly on several."
        titles.size == 1 -> titles.single()
        titles.isEmpty() -> "${selectedIds.size} tasks selected"
        else -> "${titles.size} tasks: ${titles.joinToString(", ")}"
    }
}

@Composable
private fun TaskSearchResults(
    state: TaskSearchState,
    actions: TaskSearchActions,
) {
    OutlinedTextField(state.query, actions.queryChanged, label = { Text("Search tasks") }, modifier = Modifier.fillMaxWidth())
    Text("No task", Modifier.fillMaxWidth().clickable { actions.taskSelected(null) }.padding(10.dp))
    state.tasks.filter { it.content.contains(state.query, ignoreCase = true) }.forEach { task ->
        TaskRow(task, task.id in state.selectedIds) { actions.taskSelected(task) }
    }
    state.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
}

private data class TaskSearchState(
    val tasks: List<TodoistTask>,
    val query: String,
    val selectedIds: Set<String>,
    val message: String?,
)

private data class TaskSearchActions(
    val queryChanged: (String) -> Unit,
    val taskSelected: (TodoistTask?) -> Unit,
)

@Composable
private fun TaskRow(task: TodoistTask, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Surface(color = if (task.priority > 1) Marigold else Sage, shape = CircleShape) { androidx.compose.foundation.layout.Box(Modifier.padding(5.dp)) }
        Column(Modifier.weight(1f)) {
            Text(task.content, color = Ink, style = MaterialTheme.typography.bodyMedium)
            Text(listOfNotNull(task.projectName, task.dueLabel).joinToString(" · "), color = WarmGray, style = MaterialTheme.typography.bodySmall)
        }
        if (selected) Text("Added", color = Sage, style = MaterialTheme.typography.labelSmall)
    }
}
