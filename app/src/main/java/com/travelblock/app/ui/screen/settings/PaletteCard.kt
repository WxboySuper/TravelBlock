package com.travelblock.app.ui.screen.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travelblock.app.ui.theme.ActivePalette
import com.travelblock.app.ui.theme.PaletteStore
import com.travelblock.app.ui.theme.TravelPalette
import com.travelblock.app.ui.theme.TravelPalettes

/** Settings card that lets the traveller pick the colour direction of the whole app. */
@Composable
fun PaletteCard() {
    val context = LocalContext.current
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Colors", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TravelPalettes.forEach { palette ->
                PaletteRow(palette, selected = palette.id == ActivePalette.current.id) {
                    PaletteStore.select(context, palette)
                }
            }
        }
    }
}

@Composable
private fun PaletteRow(palette: TravelPalette, selected: Boolean, onClick: () -> Unit) {
    val border = if (selected) BorderStroke(2.dp, palette.accent) else BorderStroke(1.dp, palette.sand)
    Surface(onClick = onClick, shape = RoundedCornerShape(14.dp), color = palette.cream, border = border, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Swatches(palette)
            Column(Modifier.weight(1f)) {
                Text(palette.label + if (selected) "  (in use)" else "", color = palette.ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(palette.tagline, color = palette.warmGray, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun Swatches(palette: TravelPalette) {
    Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
        listOf(palette.accent, palette.highlight, palette.duskBlue, palette.sage).forEach {
            Box(Modifier.size(22.dp).clip(CircleShape).background(it))
        }
    }
}
