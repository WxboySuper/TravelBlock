package com.travelblock.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        FlightLogEntity::class,
        PointsTransactionEntity::class,
        UnlockEntity::class,
        ActiveFlightSessionEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class TravelBlockDatabase : RoomDatabase() {
    abstract fun flightLogDao(): FlightLogDao
    abstract fun pointsTransactionDao(): PointsTransactionDao
    abstract fun unlockDao(): UnlockDao
    abstract fun activeFlightSessionDao(): ActiveFlightSessionDao
}
