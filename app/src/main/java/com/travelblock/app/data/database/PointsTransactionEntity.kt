package com.travelblock.app.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "points_transactions")
data class PointsTransactionEntity(
    @PrimaryKey val id: String,
    val amount: Int,
    val reason: String,
    val createdAt: Long,
    val relatedFlightId: String?,
)

