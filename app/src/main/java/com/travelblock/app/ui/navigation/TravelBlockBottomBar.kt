package com.travelblock.app.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travelblock.app.ui.theme.CabinAmber
import com.travelblock.app.ui.theme.DeepNavy
import com.travelblock.app.ui.theme.RunwayLine
import com.travelblock.app.ui.theme.SoftSky
import com.travelblock.app.ui.theme.TerminalBlue

@Composable
fun TravelBlockBottomBar(
    currentRoute: String?,
    destinations: List<TravelBlockDestination>,
    onDestinationSelected: (TravelBlockDestination) -> Unit,
) {
    Surface(
        color = Color.White,
        shadowElevation = 10.dp,
        border = BorderStroke(1.dp, RunwayLine),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            destinations.forEach { destination ->
                val selected = currentRoute == destination.route
                TravelDockItem(
                    destination = destination,
                    selected = selected,
                    onClick = { onDestinationSelected(destination) },
                    modifier = Modifier.weight(if (selected) 1.32f else 1f),
                )
            }
        }
    }
}

@Composable
private fun TravelDockItem(
    destination: TravelBlockDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .height(54.dp)
            .clickable(onClick = onClick),
        color = if (selected) SoftSky else Color.White,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            1.dp,
            if (selected) TerminalBlue.copy(alpha = 0.32f) else Color.Transparent,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (selected) {
                    Surface(
                        modifier = Modifier.size(28.dp),
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, RunwayLine),
                    ) {}
                }
                Icon(
                    imageVector = destination.icon,
                    contentDescription = destination.label,
                    tint = if (selected) TerminalBlue else DeepNavy.copy(alpha = 0.58f),
                    modifier = Modifier.size(19.dp),
                )
            }
            if (selected) {
                Text(
                    text = destination.label.uppercase(),
                    modifier = Modifier.padding(start = 7.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = TerminalBlue,
                    fontWeight = FontWeight.Black,
                )
                if (destination == TravelBlockDestination.Home) {
                    Text(
                        text = " .",
                        style = MaterialTheme.typography.labelSmall,
                        color = CabinAmber,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
        }
    }
}
