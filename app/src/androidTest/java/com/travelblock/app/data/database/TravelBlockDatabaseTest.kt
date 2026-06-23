package com.travelblock.app.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TravelBlockDatabaseTest {
    private lateinit var database: TravelBlockDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, TravelBlockDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertsFlightLogAndPointTransaction() = runBlocking {
        val flightId = UUID.randomUUID().toString()
        database.flightLogDao().insertFlightLog(
            FlightLogEntity(
                id = flightId,
                flightNumber = "TB123",
                originCode = "KOUN",
                destinationCode = "KOKC",
                durationMinutes = 30,
                distanceMiles = 60.0,
                status = FlightLogStatus.COMPLETED.name,
                startedAt = 1_000L,
                endedAt = 2_000L,
                seat = "18A",
                pointsEarned = 151,
            ),
        )
        database.pointsTransactionDao().insertPointTransaction(
            PointsTransactionEntity(
                id = UUID.randomUUID().toString(),
                amount = 151,
                reason = "Completed flight",
                createdAt = 2_000L,
                relatedFlightId = flightId,
            ),
        )

        assertEquals(1, database.flightLogDao().observeFlightLogsNewestFirst().first().size)
        assertEquals(151, database.pointsTransactionDao().getPointsBalance())
    }
}

