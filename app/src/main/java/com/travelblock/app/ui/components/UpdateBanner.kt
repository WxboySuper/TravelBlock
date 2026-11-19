package com.travelblock.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.travelblock.app.update.UpdateInfo
import com.travelblock.app.update.UpdateUiState

@Composable
fun UpdateBanner(state: UpdateUiState, info: UpdateInfo?, onUpdate: () -> Unit, onDismiss: () -> Unit) {
    if (info == null && state !is UpdateUiState.Error) return
    PaperCard(Modifier.padding(horizontal = 14.dp, vertical = 8.dp).fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Kicker("App update")
            info?.let { Text("Update ready - build ${it.build}", style = MaterialTheme.typography.titleMedium) }
            if (state is UpdateUiState.Error) Text(state.message)
            if (info != null) Row {
                TextButton(onClick = onUpdate) { Text("Download update") }
                TextButton(onClick = onDismiss) { Text("Dismiss") }
            }
        }
    }
}
