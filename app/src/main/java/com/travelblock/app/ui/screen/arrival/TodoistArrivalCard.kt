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

@Composable
fun TodoistArrivalCard(taskId: String?, taskContent: String?, repository: TodoistRepository?) {
    if (taskId == null || taskContent.isNullOrBlank() || repository == null) return
    var dismissed by remember(taskId) { mutableStateOf(false) }
    var completed by remember(taskId) { mutableStateOf(false) }
    var busy by remember(taskId) { mutableStateOf(false) }
    var error by remember(taskId) { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    if (dismissed) return
    PaperCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("You flew on: $taskContent", color = Ink, style = MaterialTheme.typography.titleMedium)
            if (completed) {
                Text("Done in Todoist", color = WarmGray)
            } else {
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (error?.contains("token invalid") == true) Text("Reconnect in Settings.", color = WarmGray)
                PrimaryButton(
                    text = if (busy) "Marking complete…" else if (error != null) "Retry" else "Mark task complete",
                    enabled = !busy,
                    onClick = {
                        busy = true
                        scope.launch {
                            when (val result = repository.completeTask(taskId)) {
                                is TodoistResult.Success -> { completed = true; error = null }
                                is TodoistResult.Failure -> error = result.message
                            }
                            busy = false
                        }
                    },
                )
                SecondaryButton("Not yet", onClick = { dismissed = true })
            }
        }
    }
}
