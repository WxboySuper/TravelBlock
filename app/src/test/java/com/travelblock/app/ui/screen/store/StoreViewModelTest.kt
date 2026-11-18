package com.travelblock.app.ui.screen.store

import com.travelblock.app.data.database.PointsTransactionEntity
import com.travelblock.app.data.database.UnlockEntity
import com.travelblock.app.data.repository.PointsRepository
import com.travelblock.app.data.repository.StoreRepository
import com.travelblock.app.testing.FakeSettingsRepository
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class StoreViewModelTest {
    private val fixedClock = Clock.fixed(Instant.parse("2026-05-20T18:00:00Z"), ZoneOffset.UTC)

    @Test
    fun purchaseSucceedsWithEnoughPointsAndCreatesTransaction() = runTest {
        val pointsRepository = FakePointsRepository().apply { balanceFlow.value = 200 }
        val storeRepository = FakeStoreRepository()
        val viewModel = StoreViewModel(
            pointsRepository = pointsRepository,
            storeRepository = storeRepository,
            settingsRepository = FakeSettingsRepository().repository,
            clock = fixedClock,
        )

        val result = viewModel.purchaseNow("aircraft_twin_turboprop")

        assertEquals(StorePurchaseResult.Purchased, result)
        assertNotNull(storeRepository.getUnlockByCosmeticId("aircraft_twin_turboprop"))
        assertEquals(110, pointsRepository.getPointsBalance())
        assertEquals(-90, pointsRepository.transactions.single().amount)
    }

    @Test
    fun purchaseFailsWithInsufficientPoints() = runTest {
        val pointsRepository = FakePointsRepository().apply { balanceFlow.value = 50 }
        val storeRepository = FakeStoreRepository()
        val viewModel = StoreViewModel(
            pointsRepository = pointsRepository,
            storeRepository = storeRepository,
            settingsRepository = FakeSettingsRepository().repository,
            clock = fixedClock,
        )

        val result = viewModel.purchaseNow("aircraft_twin_turboprop")

        assertEquals(StorePurchaseResult.InsufficientPoints, result)
        assertEquals(50, pointsRepository.getPointsBalance())
        assertEquals(0, pointsRepository.transactions.size)
    }

    @Test
    fun duplicatePurchaseDoesNotDoubleCharge() = runTest {
        val pointsRepository = FakePointsRepository().apply { balanceFlow.value = 300 }
        val storeRepository = FakeStoreRepository()
        val viewModel = StoreViewModel(
            pointsRepository = pointsRepository,
            storeRepository = storeRepository,
            settingsRepository = FakeSettingsRepository().repository,
            clock = fixedClock,
        )

        val first = viewModel.purchaseNow("boarding_pass_skyline")
        val second = viewModel.purchaseNow("boarding_pass_skyline")

        assertEquals(StorePurchaseResult.Purchased, first)
        assertEquals(StorePurchaseResult.AlreadyUnlocked, second)
        assertEquals(180, pointsRepository.getPointsBalance())
        assertEquals(1, pointsRepository.transactions.size)
    }

    @Test
    fun equipOnlyWorksForUnlockedItems() = runTest {
        val settingsFixture = FakeSettingsRepository()
        val storeRepository = FakeStoreRepository()
        val viewModel = StoreViewModel(
            pointsRepository = FakePointsRepository(),
            storeRepository = storeRepository,
            settingsRepository = settingsFixture.repository,
            clock = fixedClock,
        )

        val lockedResult = viewModel.equipNow("boarding_pass_skyline")
        storeRepository.insertUnlock(boardingPassUnlock())
        val unlockedResult = viewModel.equipNow("boarding_pass_skyline")

        assertEquals(StoreEquipResult.Locked, lockedResult)
        assertEquals(StoreEquipResult.Equipped, unlockedResult)
        assertEquals("boarding_pass_skyline", settingsFixture.getSettings().equippedBoardingPassThemeId)
    }

    private fun boardingPassUnlock() = UnlockEntity(
        id = "unlock-boarding_pass_skyline",
        cosmeticType = "BoardingPassTheme",
        cosmeticId = "boarding_pass_skyline",
        unlockedAt = fixedClock.instant().toEpochMilli(),
        pricePaid = 0,
    )

    private class FakePointsRepository : PointsRepository {
        val balanceFlow = MutableStateFlow(0)
        private val transactionsFlow = MutableStateFlow<List<PointsTransactionEntity>>(emptyList())
        val transactions: List<PointsTransactionEntity>
            get() = transactionsFlow.value

        override fun observePointsBalance(): Flow<Int> = balanceFlow

        override fun observeTransactions(): Flow<List<PointsTransactionEntity>> = transactionsFlow

        override suspend fun getPointsBalance(): Int = balanceFlow.value

        override suspend fun insertTransaction(transaction: PointsTransactionEntity) {
            transactionsFlow.value = listOf(transaction) + transactionsFlow.value
            balanceFlow.value += transaction.amount
        }
    }

    private class FakeStoreRepository : StoreRepository {
        private val unlocksFlow = MutableStateFlow<List<UnlockEntity>>(emptyList())

        override fun observeUnlocks(): Flow<List<UnlockEntity>> = unlocksFlow

        override suspend fun getUnlocks(): List<UnlockEntity> = unlocksFlow.value

        override suspend fun getUnlockByCosmeticId(cosmeticId: String): UnlockEntity? {
            return unlocksFlow.value.firstOrNull { it.cosmeticId == cosmeticId }
        }

        override suspend fun insertUnlock(unlock: UnlockEntity): Boolean {
            if (getUnlockByCosmeticId(unlock.cosmeticId) != null) return false
            unlocksFlow.value = listOf(unlock) + unlocksFlow.value
            return true
        }
    }

}
