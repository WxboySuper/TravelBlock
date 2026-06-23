package com.travelblock.app.data.repository

import com.travelblock.app.data.database.UnlockDao
import com.travelblock.app.data.database.UnlockEntity
import kotlinx.coroutines.flow.Flow

class DefaultStoreRepository(
    private val unlockDao: UnlockDao,
) : StoreRepository {
    override fun observeUnlocks(): Flow<List<UnlockEntity>> = unlockDao.observeUnlocks()

    override suspend fun getUnlocks(): List<UnlockEntity> = unlockDao.getUnlocks()

    override suspend fun getUnlockByCosmeticId(cosmeticId: String): UnlockEntity? {
        return unlockDao.getUnlockByCosmeticId(cosmeticId)
    }

    override suspend fun insertUnlock(unlock: UnlockEntity): Boolean {
        return unlockDao.insertUnlock(unlock) != -1L
    }
}
