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
    selectedId: String?,
    onTaskSelected: (TodoistTask?) -> Unit,
    onOpenSettings: () -> Unit,
) {
    if (repository == null) return
    val connected by repository.isConnected.collectAsState(initial = false)
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var tasks by remember { mutableStateOf(emptyList<TodoistTask>()) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    PaperCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Focus task", style = MaterialTheme.typography.titleMedium, color = Ink)
            if (!connected) {
                Text("Todoist isn't connected yet.", color = WarmGray)
                SecondaryButton("Connect Todoist in Settings", onClick = onOpenSettings)
            } else {
                Text(
                    tasks.firstOrNull { it.id == selectedId }?.content ?: "No task selected",
                    color = WarmGray,
                )
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
                    OutlinedTextField(query, { query = it }, label = { Text("Search tasks") }, modifier = Modifier.fillMaxWidth())
                    Text("No task", Modifier.fillMaxWidth().clickable { onTaskSelected(null) }.padding(10.dp))
                    tasks.filter { it.content.contains(query, ignoreCase = true) }.forEach { task ->
                        TaskRow(task, selectedId == task.id) { onTaskSelected(task) }
                    }
                    message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }
}

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
        if (selected) Text("Selected", color = Sage, style = MaterialTheme.typography.labelSmall)
    }
}
