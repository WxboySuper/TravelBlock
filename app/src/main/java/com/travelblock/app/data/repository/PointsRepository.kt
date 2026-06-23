package com.travelblock.app.data.repository

import com.travelblock.app.data.database.PointsTransactionEntity
import kotlinx.coroutines.flow.Flow

interface PointsRepository {
    fun observePointsBalance(): Flow<Int>
    fun observeTransactions(): Flow<List<PointsTransactionEntity>>
    suspend fun getPointsBalance(): Int
    suspend fun insertTransaction(transaction: PointsTransactionEntity)
}

