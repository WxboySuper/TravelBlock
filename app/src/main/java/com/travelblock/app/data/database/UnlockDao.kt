package com.travelblock.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UnlockDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUnlock(unlock: UnlockEntity): Long

    @Query("SELECT * FROM unlocked_cosmetics ORDER BY unlockedAt DESC")
    fun observeUnlocks(): Flow<List<UnlockEntity>>

    @Query("SELECT * FROM unlocked_cosmetics ORDER BY unlockedAt DESC")
    suspend fun getUnlocks(): List<UnlockEntity>

    @Query("SELECT * FROM unlocked_cosmetics WHERE cosmeticId = :cosmeticId LIMIT 1")
    suspend fun getUnlockByCosmeticId(cosmeticId: String): UnlockEntity?
}
