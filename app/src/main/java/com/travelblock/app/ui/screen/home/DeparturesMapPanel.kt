package com.travelblock.app.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.travelblock.app.domain.model.GeoPoint
import com.travelblock.app.ui.components.DeparturePin
import com.travelblock.app.ui.components.DeparturesMap
import com.travelblock.app.ui.components.DeparturesMapModel
import com.travelblock.app.ui.components.MapStop
import com.travelblock.app.ui.theme.Lamp

/** The List / Map switch in the departures board header. */
@Composable
fun BoardViewToggle(showMap: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        Modifier.clip(RoundedCornerShape(50)).background(Color(0xFF2B2938)).padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        ToggleLabel("List", selected = !showMap) { onToggle(false) }
        ToggleLabel("Map", selected = showMap) { onToggle(true) }
    }
}

@Composable
private fun ToggleLabel(text: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = if (selected) Color(0xFF1C1B22) else Lamp,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) Lamp else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 5.dp),
    )
}

/** Every departure on the board as a pin on a real map; tapping a pin selects that destination. */
@Composable
fun DeparturesMapPanel(uiState: HomeUiState, onRouteSelected: (String) -> Unit) {
    val first = uiState.routes.firstOrNull()
    if (first == null) return
    val model = remember(uiState.routes, uiState.selectedRouteId) {
        DeparturesMapModel(
            origin = MapStop(first.originCode, GeoPoint(first.originLatitude, first.originLongitude)),
            pins = uiState.routes.map { DeparturePin(it.id, it.destinationCode, GeoPoint(it.destinationLatitude, it.destinationLongitude)) },
            selectedId = uiState.selectedRouteId,
        )
    }
    DeparturesMap(
        model = model,
        onPinSelected = onRouteSelected,
        modifier = Modifier.fillMaxWidth().height(340.dp).clip(RoundedCornerShape(18.dp)),
    )
}
