package com.travelblock.app.data.repository

import com.travelblock.app.data.database.PointsTransactionDao
import com.travelblock.app.data.database.PointsTransactionEntity
import kotlinx.coroutines.flow.Flow

class DefaultPointsRepository(
    private val pointsTransactionDao: PointsTransactionDao,
) : PointsRepository {
    override fun observePointsBalance(): Flow<Int> {
        return pointsTransactionDao.observePointsBalance()
    }

    override fun observeTransactions(): Flow<List<PointsTransactionEntity>> {
        return pointsTransactionDao.observeTransactionsNewestFirst()
    }

    override suspend fun getPointsBalance(): Int {
        return pointsTransactionDao.getPointsBalance()
    }

    override suspend fun insertTransaction(transaction: PointsTransactionEntity) {
        pointsTransactionDao.insertPointTransaction(transaction)
    }
}

