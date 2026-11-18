package com.travelblock.app.ui.screen.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.travelblock.app.ui.components.PaperCard
import com.travelblock.app.ui.theme.Cream
import com.travelblock.app.ui.theme.Ink
import com.travelblock.app.ui.theme.WarmGray

@Composable
fun PlaceholderScreen(
    title: String,
    message: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        PaperCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(22.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = title, style = MaterialTheme.typography.headlineSmall, color = Ink)
                Text(text = message, style = MaterialTheme.typography.bodyLarge, color = WarmGray)
            }
        }
    }
}
