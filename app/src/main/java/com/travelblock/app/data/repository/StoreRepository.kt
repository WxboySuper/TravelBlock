package com.travelblock.app.data.repository

import com.travelblock.app.data.database.UnlockEntity
import kotlinx.coroutines.flow.Flow

interface StoreRepository {
    fun observeUnlocks(): Flow<List<UnlockEntity>>
    suspend fun getUnlocks(): List<UnlockEntity>
    suspend fun getUnlockByCosmeticId(cosmeticId: String): UnlockEntity?
    suspend fun insertUnlock(unlock: UnlockEntity): Boolean
}
