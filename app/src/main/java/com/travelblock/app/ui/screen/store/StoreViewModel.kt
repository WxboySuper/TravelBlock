package com.travelblock.app.ui.screen.store

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelblock.app.data.database.PointsTransactionEntity
import com.travelblock.app.data.database.UnlockEntity
import com.travelblock.app.data.repository.PointsRepository
import com.travelblock.app.data.repository.StoreRepository
import com.travelblock.app.data.settings.SettingsRepository
import com.travelblock.app.data.settings.UserSettings
import com.travelblock.app.domain.engine.StoreCatalog
import com.travelblock.app.domain.model.StoreItem
import com.travelblock.app.domain.model.StoreItemCategory
import java.time.Clock
import java.util.UUID
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StoreViewModel(
    private val pointsRepository: PointsRepository,
    private val storeRepository: StoreRepository,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {
    var uiState by mutableStateOf(StoreUiState())
        private set

    private var pointsBalance: Int = 0
    private var unlockedIds: Set<String> = emptySet()
    private var settings = UserSettings()

    init {
        viewModelScope.launch {
            pointsRepository.observePointsBalance().collectLatest { balance ->
                pointsBalance = balance
                rebuildState()
            }
        }
        viewModelScope.launch {
            storeRepository.observeUnlocks().collectLatest { unlocks ->
                unlockedIds = unlocks.map { it.cosmeticId }.toSet()
                rebuildState()
            }
        }
        viewModelScope.launch {
            settingsRepository.settings.collectLatest { userSettings ->
                settings = userSettings
                rebuildState()
            }
        }
    }

    fun purchase(itemId: String) {
        viewModelScope.launch {
            when (purchaseNow(itemId)) {
                StorePurchaseResult.Purchased -> setMessage("Unlocked ${itemName(itemId)}.")
                StorePurchaseResult.AlreadyUnlocked -> setMessage("${itemName(itemId)} is already unlocked.")
                StorePurchaseResult.InsufficientPoints -> setMessage("Not enough points for ${itemName(itemId)}.")
                StorePurchaseResult.UnknownItem -> setMessage("Store item not found.")
            }
        }
    }

    fun equip(itemId: String) {
        viewModelScope.launch {
            when (equipNow(itemId)) {
                StoreEquipResult.Equipped -> setMessage("Equipped ${itemName(itemId)}.")
                StoreEquipResult.Locked -> setMessage("Unlock ${itemName(itemId)} before equipping it.")
                StoreEquipResult.Unsupported -> setMessage("This perk does not need equipping yet.")
            }
        }
    }

    suspend fun purchaseNow(itemId: String): StorePurchaseResult {
        val item = StoreCatalog.items.firstOrNull { it.id == itemId } ?: return StorePurchaseResult.UnknownItem
        if (storeRepository.getUnlockByCosmeticId(itemId) != null) return StorePurchaseResult.AlreadyUnlocked
        val balance = pointsRepository.getPointsBalance()
        if (balance < item.cost) return StorePurchaseResult.InsufficientPoints

        val now = clock.instant().toEpochMilli()
        val unlockInserted = storeRepository.insertUnlock(
            UnlockEntity(
                id = UUID.randomUUID().toString(),
                cosmeticType = item.category.name,
                cosmeticId = item.id,
                unlockedAt = now,
                pricePaid = item.cost,
            ),
        )
        if (!unlockInserted) return StorePurchaseResult.AlreadyUnlocked

        pointsRepository.insertTransaction(
            PointsTransactionEntity(
                id = UUID.randomUUID().toString(),
                amount = -item.cost,
                reason = "Unlocked ${item.name}",
                createdAt = now,
                relatedFlightId = null,
            ),
        )
        return StorePurchaseResult.Purchased
    }

    suspend fun equipNow(itemId: String): StoreEquipResult {
        val item = StoreCatalog.items.firstOrNull { it.id == itemId } ?: return StoreEquipResult.Unsupported
        if (storeRepository.getUnlockByCosmeticId(itemId) == null) return StoreEquipResult.Locked
        when (item.category) {
            StoreItemCategory.BoardingPassTheme -> settingsRepository.setEquippedBoardingPassThemeId(item.id)
            StoreItemCategory.AircraftIcon -> settingsRepository.setEquippedAircraftIconId(item.id)
            StoreItemCategory.CabinAmbience -> return StoreEquipResult.Unsupported
        }
        return StoreEquipResult.Equipped
    }

    private fun rebuildState() {
        uiState = uiState.copy(
            pointsBalance = pointsBalance,
            items = StoreCatalog.items.map { it.toUiModel() },
            isLoading = false,
        )
    }

    private fun StoreItem.toUiModel(): StoreItemUiModel {
        val unlocked = id in unlockedIds
        val equipped = when (category) {
            StoreItemCategory.BoardingPassTheme -> settings.equippedBoardingPassThemeId == id
            StoreItemCategory.AircraftIcon -> settings.equippedAircraftIconId == id
            StoreItemCategory.CabinAmbience -> false
        }
        return StoreItemUiModel(
            id = id,
            name = name,
            description = description,
            category = category,
            costLabel = "$cost pts",
            unlocked = unlocked,
            equipped = equipped,
            canAfford = pointsBalance >= cost,
        )
    }

    private fun setMessage(message: String) {
        uiState = uiState.copy(message = message)
    }

    private fun itemName(itemId: String): String {
        return StoreCatalog.items.firstOrNull { it.id == itemId }?.name ?: "this item"
    }
}
