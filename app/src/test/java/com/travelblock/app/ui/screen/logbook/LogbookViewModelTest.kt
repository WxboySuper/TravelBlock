package com.travelblock.app.ui.screen.logbook

import com.travelblock.app.data.database.FlightLogEntity
import com.travelblock.app.data.database.FlightLogStatus
import com.travelblock.app.data.repository.FlightLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LogbookViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun summaryStatsAreCalculatedFromSampleLogs() = runTest(dispatcher) {
        val repository = FakeFlightLogRepository(
            listOf(
                flightLog("old", FlightLogStatus.COMPLETED, durationMinutes = 30, distanceMiles = 60.4, points = 151, endedAt = 1_000),
                flightLog("new", FlightLogStatus.DIVERTED, durationMinutes = 12, distanceMiles = 20.2, points = 10, endedAt = 2_000),
            ),
        )

        val viewModel = LogbookViewModel(repository)
        advanceUntilIdle()

        assertEquals("2", viewModel.uiState.summary.totalFlightsLabel)
        assertEquals("1", viewModel.uiState.summary.completedFlightsLabel)
        assertEquals("1", viewModel.uiState.summary.divertedFlightsLabel)
        assertEquals("42m", viewModel.uiState.summary.totalFocusTimeLabel)
        assertEquals("81 mi", viewModel.uiState.summary.totalMilesLabel)
        assertEquals("161 pts", viewModel.uiState.summary.totalPointsLabel)
    }

    @Test
    fun entriesAreSortedNewestFirst() = runTest(dispatcher) {
        val repository = FakeFlightLogRepository(
            listOf(
                flightLog("old", FlightLogStatus.COMPLETED, endedAt = 1_000),
                flightLog("new", FlightLogStatus.COMPLETED, endedAt = 3_000),
                flightLog("middle", FlightLogStatus.DIVERTED, endedAt = 2_000),
            ),
        )

        val viewModel = LogbookViewModel(repository)
        advanceUntilIdle()

        assertEquals(listOf("new", "middle", "old"), viewModel.uiState.entries.map { it.id })
    }

    @Test
    fun completedAndDivertedEntriesAreDistinguished() = runTest(dispatcher) {
        val repository = FakeFlightLogRepository(
            listOf(
                flightLog("completed", FlightLogStatus.COMPLETED),
                flightLog("diverted", FlightLogStatus.DIVERTED),
            ),
        )

        val viewModel = LogbookViewModel(repository)
        advanceUntilIdle()

        val completed = viewModel.uiState.entries.first { it.id == "completed" }
        val diverted = viewModel.uiState.entries.first { it.id == "diverted" }
        assertTrue(completed.isCompleted)
        assertFalse(diverted.isCompleted)
        assertEquals("Completed", completed.statusLabel)
        assertEquals("Diverted", diverted.statusLabel)
    }

    @Test
    fun detailLookupByIdLoadsFlight() = runTest(dispatcher) {
        val repository = FakeFlightLogRepository(
            listOf(flightLog("target", FlightLogStatus.COMPLETED, seat = "8A")),
        )

        val viewModel = FlightLogDetailViewModel("target", repository)
        advanceUntilIdle()

        assertEquals("target", viewModel.uiState.flight?.id)
        assertEquals("8A", viewModel.uiState.flight?.seatLabel)
        assertEquals("Completed", viewModel.uiState.flight?.statusLabel)
    }

    private class FakeFlightLogRepository(
        initialLogs: List<FlightLogEntity>,
    ) : FlightLogRepository {
        private val logs = initialLogs.toMutableList()
        private val flow = MutableStateFlow(logs.toList())

        override fun observeFlightLogs(): Flow<List<FlightLogEntity>> = flow

        override suspend fun getFlightLogs(): List<FlightLogEntity> = logs

        override suspend fun getFlightById(id: String): FlightLogEntity? = logs.firstOrNull { it.id == id }

        override suspend fun insertFlightLog(flightLog: FlightLogEntity) {
            logs += flightLog
            flow.value = logs.toList()
        }
    }

    private fun flightLog(
        id: String,
        status: FlightLogStatus,
        durationMinutes: Int = 30,
        distanceMiles: Double = 60.0,
        points: Int = 151,
        endedAt: Long = 1_000,
        seat: String = "12C",
    ): FlightLogEntity {
        return FlightLogEntity(
            id = id,
            flightNumber = "TB$id",
            originCode = "KOUN",
            destinationCode = "KOKC",
            durationMinutes = durationMinutes,
            distanceMiles = distanceMiles,
            status = status.name,
            startedAt = endedAt - durationMinutes * 60_000L,
            endedAt = endedAt,
            seat = seat,
            pointsEarned = points,
        )
    }
}
