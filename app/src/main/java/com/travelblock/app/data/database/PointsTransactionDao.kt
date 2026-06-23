package com.travelblock.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PointsTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPointTransaction(transaction: PointsTransactionEntity)

    @Query("SELECT * FROM points_transactions ORDER BY createdAt DESC")
    fun observeTransactionsNewestFirst(): Flow<List<PointsTransactionEntity>>

    @Query("SELECT * FROM points_transactions ORDER BY createdAt DESC")
    suspend fun getTransactionsNewestFirst(): List<PointsTransactionEntity>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM points_transactions")
    fun observePointsBalance(): Flow<Int>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM points_transactions")
    suspend fun getPointsBalance(): Int
}

