package com.travelblock.app.ui.screen.store

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.travelblock.app.TravelBlockContainer
import com.travelblock.app.domain.model.StoreItemCategory
import com.travelblock.app.ui.components.Kicker
import com.travelblock.app.ui.components.PaperCard
import com.travelblock.app.ui.components.StampPill
import com.travelblock.app.ui.screen.logbook.SectionTitle
import com.travelblock.app.ui.theme.Cream
import com.travelblock.app.ui.theme.DuskBlue
import com.travelblock.app.ui.theme.Ink
import com.travelblock.app.ui.theme.Marigold
import com.travelblock.app.ui.theme.Paper
import com.travelblock.app.ui.theme.Sage
import com.travelblock.app.ui.theme.Sand
import com.travelblock.app.ui.theme.Terracotta
import com.travelblock.app.ui.theme.WarmGray

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
        modifier = Modifier.fillMaxSize().background(Cream).statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { StoreHeader() }
        item { WalletCard(uiState.pointsBalance, uiState.message) }
        categorySections(uiState, onPurchase, onEquip)
        item { SeatUpgradeNote() }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

private fun LazyListScope.categorySections(
    uiState: StoreUiState,
    onPurchase: (String) -> Unit,
    onEquip: (String) -> Unit,
) {
    StoreItemCategory.entries.forEach { category ->
        val items = uiState.items.filter { it.category == category }
        if (items.isEmpty()) return@forEach
        item { SectionTitle(category.title(), category.tagline()) }
        items(items, key = { it.id }) { item -> SouvenirCard(item, onPurchase, onEquip) }
    }
}

@Composable
private fun StoreHeader() {
    Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Kicker("Duty free")
        Text("Souvenir shop", style = MaterialTheme.typography.headlineLarge, color = Ink)
        Text("Spend the points you've earned on little comforts for your journeys.", style = MaterialTheme.typography.bodyMedium, color = WarmGray)
    }
}

@Composable
private fun WalletCard(pointsBalance: Int, message: String?) {
    PaperCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Kicker("Your points")
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("$pointsBalance", style = MaterialTheme.typography.displayMedium, color = DuskBlue)
                Text("pts", style = MaterialTheme.typography.titleMedium, color = WarmGray, modifier = Modifier.padding(bottom = 8.dp))
            }
            message?.let { MessageBanner(it) }
        }
    }
}

@Composable
private fun MessageBanner(message: String) {
    Surface(color = Sage.copy(alpha = 0.16f), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Sage.copy(alpha = 0.45f))) {
        Text(message, modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), style = MaterialTheme.typography.bodyMedium, color = Ink)
    }
}

@Composable
private fun SouvenirCard(item: StoreItemUiModel, onPurchase: (String) -> Unit, onEquip: (String) -> Unit) {
    PaperCard(Modifier.fillMaxWidth(), selected = item.equipped, contentPadding = PaddingValues(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
            Medallion(item.category.icon(), owned = item.unlocked)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(item.name, style = MaterialTheme.typography.titleLarge, color = Ink)
                Text(item.description, style = MaterialTheme.typography.bodyMedium, color = WarmGray)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    ActionButton(item, onPurchase, onEquip)
                    StateStamp(item)
                }
            }
        }
    }
}

@Composable
private fun Medallion(icon: ImageVector, owned: Boolean) {
    Box(
        Modifier
            .size(52.dp)
            .background(if (owned) Marigold.copy(alpha = 0.25f) else Sand.copy(alpha = 0.55f), CircleShape)
            .border(BorderStroke(2.dp, if (owned) Marigold else Sand), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = if (owned) Ink else WarmGray, modifier = Modifier.size(26.dp))
    }
}

@Composable
private fun ActionButton(item: StoreItemUiModel, onPurchase: (String) -> Unit, onEquip: (String) -> Unit) {
    when {
        item.equipped -> PillButton("Equipped", enabled = false, filled = false) {}
        item.unlocked -> PillButton("Equip", enabled = true, filled = false) { onEquip(item.id) }
        else -> PillButton("Unlock ${item.costLabel}", enabled = true, filled = item.canAfford) { onPurchase(item.id) }
    }
}

@Composable
private fun StateStamp(item: StoreItemUiModel) {
    when {
        item.equipped -> StampPill("In use", color = Sage)
        item.unlocked -> StampPill("Owned", color = DuskBlue)
        !item.canAfford -> StampPill("Keep flying", color = Terracotta)
    }
}

/** Compact pill button; an unaffordable unlock stays tappable so the shop can explain what's missing. */
@Composable
private fun PillButton(text: String, enabled: Boolean, filled: Boolean, onClick: () -> Unit) {
    val container = if (filled) Terracotta else Paper
    val content = if (filled) Paper else if (enabled) DuskBlue else WarmGray
    Surface(
        modifier = Modifier.height(42.dp).clip(RoundedCornerShape(21.dp)).clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(21.dp),
        color = container,
        border = if (filled) null else BorderStroke(1.5.dp, Sand),
    ) {
        Box(Modifier.padding(horizontal = 18.dp), contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.labelLarge, color = content)
        }
    }
}

@Composable
private fun SeatUpgradeNote() {
    PaperCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Seat upgrades", style = MaterialTheme.typography.titleLarge, color = Ink)
            Text(
                "Premium, window, and extra-legroom seats require enough points to select. Points are not deducted until a later booking-confirmation phase.",
                color = WarmGray,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun StoreItemCategory.title(): String = when (this) {
    StoreItemCategory.BoardingPassTheme -> "Boarding pass themes"
    StoreItemCategory.AircraftIcon -> "Aircraft icons"
    StoreItemCategory.CabinAmbience -> "Cabin ambience packs"
}

private fun StoreItemCategory.tagline(): String = when (this) {
    StoreItemCategory.BoardingPassTheme -> "Give your ticket a new look."
    StoreItemCategory.AircraftIcon -> "Pick the plane on your map."
    StoreItemCategory.CabinAmbience -> "Set the mood for takeoff and cruise."
}

private fun StoreItemCategory.icon(): ImageVector = when (this) {
    StoreItemCategory.BoardingPassTheme -> Icons.Rounded.ConfirmationNumber
    StoreItemCategory.AircraftIcon -> Icons.Rounded.Flight
    StoreItemCategory.CabinAmbience -> Icons.Rounded.Headphones
}
