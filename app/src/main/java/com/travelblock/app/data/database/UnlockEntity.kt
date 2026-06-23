package com.travelblock.app.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unlocked_cosmetics")
data class UnlockEntity(
    @PrimaryKey val id: String,
    val cosmeticType: String,
    val cosmeticId: String,
    val unlockedAt: Long,
    val pricePaid: Int,
)
