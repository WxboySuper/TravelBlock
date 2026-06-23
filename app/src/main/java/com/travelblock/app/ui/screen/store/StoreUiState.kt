package com.travelblock.app.ui.screen.store

import com.travelblock.app.domain.model.StoreItemCategory

data class StoreUiState(
    val pointsBalance: Int = 0,
    val items: List<StoreItemUiModel> = emptyList(),
    val message: String? = null,
    val isLoading: Boolean = true,
)

data class StoreItemUiModel(
    val id: String,
    val name: String,
    val description: String,
    val category: StoreItemCategory,
    val costLabel: String,
    val unlocked: Boolean,
    val equipped: Boolean,
    val canAfford: Boolean,
)

enum class StorePurchaseResult {
    Purchased,
    AlreadyUnlocked,
    InsufficientPoints,
    UnknownItem,
}

enum class StoreEquipResult {
    Equipped,
    Locked,
    Unsupported,
}
