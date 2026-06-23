package com.travelblock.app

import android.content.Context
import androidx.room.Room
import com.travelblock.app.data.database.MIGRATION_2_3
import com.travelblock.app.data.database.TravelBlockDatabase
import com.travelblock.app.data.database.MIGRATION_3_4
import com.travelblock.app.data.local.AirportAssetDataSource
import com.travelblock.app.data.repository.ActiveFlightSessionRepository
import com.travelblock.app.data.repository.DefaultAirportRepository
import com.travelblock.app.data.repository.DefaultActiveFlightSessionRepository
import com.travelblock.app.data.repository.DefaultFlightLogRepository
import com.travelblock.app.data.repository.DefaultPointsRepository
import com.travelblock.app.data.repository.DefaultStoreRepository
import com.travelblock.app.data.repository.FlightLogRepository
import com.travelblock.app.data.repository.PointsRepository
import com.travelblock.app.data.repository.StoreRepository
import com.travelblock.app.data.settings.DataStoreSettingsRepository
import com.travelblock.app.data.settings.SettingsRepository
import com.travelblock.app.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TravelBlockContainer(
    context: Context,
) {
    private val appContext = context.applicationContext

    val database: TravelBlockDatabase by lazy {
        Room.databaseBuilder(
            appContext,
            TravelBlockDatabase::class.java,
            "travelblock.db",
        ).addMigrations(
            MIGRATION_2_3,
            MIGRATION_3_4,
        ).build()
    }

    val settingsRepository: SettingsRepository by lazy {
        DataStoreSettingsRepository(appContext)
    }

    val airportRepository: DefaultAirportRepository by lazy {
        DefaultAirportRepository(
            airportProvider = { AirportAssetDataSource(appContext).loadAirports() },
        )
    }

    val flightLogRepository: FlightLogRepository by lazy {
        DefaultFlightLogRepository(database.flightLogDao())
    }

    val pointsRepository: PointsRepository by lazy {
        DefaultPointsRepository(database.pointsTransactionDao())
    }

    val storeRepository: StoreRepository by lazy {
        DefaultStoreRepository(database.unlockDao())
    }

    val activeFlightSessionRepository: ActiveFlightSessionRepository by lazy {
        DefaultActiveFlightSessionRepository(database.activeFlightSessionDao())
    }

    val notificationHelper: NotificationHelper by lazy {
        NotificationHelper(appContext)
    }

    suspend fun resetLocalData() {
        withContext(Dispatchers.IO) {
            database.clearAllTables()
        }
        settingsRepository.resetToDefaults()
    }
}
