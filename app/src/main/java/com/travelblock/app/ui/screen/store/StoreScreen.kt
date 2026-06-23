package com.travelblock.app.ui.screen.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelblock.app.TravelBlockContainer
import com.travelblock.app.domain.model.StoreItemCategory
import com.travelblock.app.ui.components.PointsPill
import com.travelblock.app.ui.components.SectionHeader
import com.travelblock.app.ui.components.StatusChip
import com.travelblock.app.ui.components.StatusTone
import com.travelblock.app.ui.theme.CabinAmber
import com.travelblock.app.ui.theme.MutedText
import com.travelblock.app.ui.theme.SoftSky

@Composable
fun StoreScreen(container: TravelBlockContainer) {
    val viewModel: StoreViewModel = viewModel(
        factory = StoreViewModelFactory(
            pointsRepository = container.pointsRepository,
            storeRepository = container.storeRepository,
            settingsRepository = container.settingsRepository,
        ),
    )

    StoreScreenContent(
        uiState = viewModel.uiState,
        onPurchase = viewModel::purchase,
        onEquip = viewModel::equip,
    )
}

@Composable
fun StoreScreenContent(
    uiState: StoreUiState,
    onPurchase: (String) -> Unit,
    onEquip: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            SectionHeader(
                title = "Store",
                subtitle = "Airline perks and cabin upgrades using earned points only.",
            )
        }
        item {
            PointsCard(uiState.pointsBalance, uiState.message)
        }
        StoreItemCategory.entries.forEach { category ->
            val items = uiState.items.filter { it.category == category }
            if (items.isNotEmpty()) {
                item {
                    SectionHeader(title = category.toTitle())
                }
                items(items, key = { it.id }) { item ->
                    StoreItemCard(item, onPurchase, onEquip)
                }
            }
        }
        item {
            SeatUpgradeInfoCard()
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PointsCard(
    pointsBalance: Int,
    message: String?,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Points balance", color = MutedText, style = MaterialTheme.typography.labelLarge)
            PointsPill(points = pointsBalance)
            message?.let {
                Surface(color = SoftSky, shape = RoundedCornerShape(14.dp)) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun StoreItemCard(
    item: StoreItemUiModel,
    onPurchase: (String) -> Unit,
    onEquip: (String) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(item.description, style = MaterialTheme.typography.bodyMedium, color = MutedText)
                }
                StatusPill(item)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (item.unlocked) {
                    OutlinedButton(onClick = { onEquip(item.id) }, enabled = !item.equipped) {
                        Text(if (item.equipped) "Equipped" else "Equip")
                    }
                } else {
                    Button(onClick = { onPurchase(item.id) }) {
                        Text("Unlock ${item.costLabel}")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(item: StoreItemUiModel) {
    val text = when {
        item.equipped -> "Equipped"
        item.unlocked -> "Unlocked"
        else -> item.costLabel
    }
    StatusChip(
        text = text,
        tone = if (item.unlocked) StatusTone.Success else StatusTone.Warning,
    )
}

@Composable
private fun SeatUpgradeInfoCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Seat upgrades", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Premium, window, and extra-legroom seats require enough points to select. Points are not deducted until a later booking-confirmation phase.",
                color = MutedText,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun StoreItemCategory.toTitle(): String {
    return when (this) {
        StoreItemCategory.BoardingPassTheme -> "Boarding pass themes"
        StoreItemCategory.AircraftIcon -> "Aircraft icons"
        StoreItemCategory.CabinAmbience -> "Cabin ambience packs"
    }
}
