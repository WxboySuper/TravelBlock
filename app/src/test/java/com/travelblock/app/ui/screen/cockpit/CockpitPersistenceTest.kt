package com.travelblock.app.ui.screen.cockpit

import com.travelblock.app.data.database.FlightLogEntity
import com.travelblock.app.data.database.FlightLogStatus
import com.travelblock.app.data.database.PointsTransactionEntity
import com.travelblock.app.data.repository.FlightLogRepository
import com.travelblock.app.data.repository.ActiveFlightSessionRepository
import com.travelblock.app.data.repository.PointsRepository
import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.ActiveFlightStatus
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.SeatOption
import com.travelblock.app.domain.model.TicketClass
import com.travelblock.app.testing.FakeSettingsRepository
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CockpitPersistenceTest {
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
    fun completedFlightInsertsLogPointsAndUpdatesCurrentAirport() = runTest(dispatcher) {
        val clock = MutableClock(start.plusSeconds(31 * 60))
        val flightLogs = FakeFlightLogRepository()
        val points = FakePointsRepository()
        val settings = FakeSettingsRepository()
        val viewModel = CockpitViewModel(activeFlight, clock, ZoneId.of("UTC"), flightLogs, points, settings.repository)

        viewModel.tick()
        advanceUntilIdle()

        assertEquals(1, flightLogs.logs.size)
        assertEquals(FlightLogStatus.COMPLETED.name, flightLogs.logs.single().status)
        assertEquals(37, flightLogs.logs.single().pointsEarned)
        assertEquals(1, points.transactions.size)
        assertEquals(37, points.transactions.single().amount)
        assertEquals("KOKC", settings.currentAirportCode)
    }

    @Test
    fun completedFlightOnlyPersistsOnceWhenTickRepeats() = runTest(dispatcher) {
        val clock = MutableClock(start.plusSeconds(31 * 60))
        val flightLogs = FakeFlightLogRepository()
        val points = FakePointsRepository()
        val settings = FakeSettingsRepository()
        val viewModel = CockpitViewModel(activeFlight, clock, ZoneId.of("UTC"), flightLogs, points, settings.repository)

        viewModel.tick()
        viewModel.tick()
        advanceUntilIdle()

        assertEquals(1, flightLogs.logs.size)
        assertEquals(1, points.transactions.size)
        assertEquals("KOKC", settings.currentAirportCode)
    }

    @Test
    fun itineraryLogsEachLegAndAwardsBonusOnlyAfterFinalArrival() = runTest(dispatcher) {
        val clock = MutableClock(start.plusSeconds(30 * 60))
        val flightLogs = FakeFlightLogRepository()
        val points = FakePointsRepository()
        val settings = FakeSettingsRepository()
        val sessions = FakeActiveFlightSessionRepository()
        val airports = listOf(activeFlight.origin, activeFlight.destination, TestAirports.tulsa)
        val tripFlight = activeFlight.copy(
            itineraryCodes = "KOUN,KOKC,KTUL",
            itineraryLabel = "Domestic",
            itineraryLegCount = 2,
            itineraryDistanceMiles = 120.0,
            todoistTaskId = "task-1",
            todoistTaskContent = "Write report",
        )
        val viewModel = CockpitViewModel(
            tripFlight, clock, ZoneId.of("UTC"), flightLogs, points, settings.repository, sessions, airports,
        )

        viewModel.tick()
        advanceUntilIdle()
        assertEquals(ActiveFlightStatus.Layover, viewModel.uiState.status)
        assertEquals(ActiveFlightStatus.Layover, sessions.flight?.status)
        assertEquals(clock.millis() + 5 * 60 * 1_000, viewModel.uiState.activeFlight?.layoverEndsAtEpochMillis)
        assertEquals("task-1", flightLogs.logs.single().todoistTaskId)
        assertEquals(1, viewModel.uiState.activeFlight?.itineraryLegIndex?.plus(1))

        viewModel.skipLayover()
        advanceUntilIdle()
        viewModel.boardNextLeg()
        advanceUntilIdle()
        assertEquals(ActiveFlightStatus.Active, viewModel.uiState.status)
        assertEquals("KOKC", viewModel.uiState.activeFlight?.origin?.code)
        assertEquals("KTUL", viewModel.uiState.activeFlight?.destination?.code)

        clock.current = clock.instant().plusSeconds(31 * 60)
        viewModel.tick()
        advanceUntilIdle()

        assertEquals(2, flightLogs.logs.size)
        assertEquals(3, points.transactions.size)
        assertTrue(points.transactions.any { it.reason == "Completed long-haul trip" && it.amount == 20 })
        assertNull(sessions.flight)
    }

    @Test
    fun diversionOnLaterLegKeepsCompletedLegAndDoesNotAwardTripBonus() = runTest(dispatcher) {
        val clock = MutableClock(start.plusSeconds(30 * 60))
        val flightLogs = FakeFlightLogRepository()
        val points = FakePointsRepository()
        val sessions = FakeActiveFlightSessionRepository()
        val airports = listOf(activeFlight.origin, activeFlight.destination, TestAirports.tulsa)
        val tripFlight = activeFlight.copy(itineraryCodes = "KOUN,KOKC,KTUL", itineraryLegCount = 2)
        val viewModel = CockpitViewModel(
            tripFlight, clock, ZoneId.of("UTC"), flightLogs, points, FakeSettingsRepository().repository,
            sessions, airports,
        )

        viewModel.tick()
        advanceUntilIdle()
        viewModel.skipLayover()
        advanceUntilIdle()
        viewModel.boardNextLeg()
        advanceUntilIdle()
        clock.current = clock.instant().plusSeconds(5 * 60)
        viewModel.confirmDivert()
        advanceUntilIdle()

        assertEquals(listOf("COMPLETED", "DIVERTED"), flightLogs.logs.map { it.status })
        assertTrue(points.transactions.none { it.reason == "Completed long-haul trip" })
        assertNull(sessions.flight)
    }

    @Test
    fun divertedFlightInsertsDivertedLogWithoutMovingCurrentAirport() = runTest(dispatcher) {
        val clock = MutableClock(start.plusSeconds(5 * 60))
        val flightLogs = FakeFlightLogRepository()
        val points = FakePointsRepository()
        val settings = FakeSettingsRepository()
        val viewModel = CockpitViewModel(activeFlight, clock, ZoneId.of("UTC"), flightLogs, points, settings.repository)

        viewModel.confirmDivert()
        advanceUntilIdle()

        assertEquals(1, flightLogs.logs.size)
        assertEquals(FlightLogStatus.DIVERTED.name, flightLogs.logs.single().status)
        assertEquals(5, flightLogs.logs.single().durationMinutes)
        assertEquals(0, flightLogs.logs.single().pointsEarned)
        assertEquals("KOUN", settings.currentAirportCode)
    }

    @Test
    fun laterDivertedFlightCanEarnReducedPointsAndFallsBackToOriginWithoutAirportList() = runTest(dispatcher) {
        val clock = MutableClock(start.plusSeconds(15 * 60))
        val flightLogs = FakeFlightLogRepository()
        val points = FakePointsRepository()
        val settings = FakeSettingsRepository()
        val viewModel = CockpitViewModel(activeFlight, clock, ZoneId.of("UTC"), flightLogs, points, settings.repository)

        viewModel.confirmDivert()
        advanceUntilIdle()

        assertEquals(1, flightLogs.logs.size)
        assertEquals(FlightLogStatus.DIVERTED.name, flightLogs.logs.single().status)
        assertEquals(15, flightLogs.logs.single().durationMinutes)
        assertEquals(9, flightLogs.logs.single().pointsEarned)
        assertEquals(9, points.transactions.single().amount)
        assertEquals("KOUN", settings.currentAirportCode)
    }

    @Test
    fun divertedFlightMovesCurrentAirportToNearestDiversionAirport() = runTest(dispatcher) {
        val clock = MutableClock(start.plusSeconds(15 * 60))
        val flightLogs = FakeFlightLogRepository()
        val points = FakePointsRepository()
        val settings = FakeSettingsRepository()
        val diversionAirport = TestAirports.wileyPost
        val viewModel = CockpitViewModel(
            activeFlight = activeFlight,
            clock = clock,
            zoneId = ZoneId.of("UTC"),
            flightLogRepository = flightLogs,
            pointsRepository = points,
            settingsRepository = settings.repository,
            airports = listOf(
                activeFlight.origin,
                activeFlight.destination,
                diversionAirport,
                TestAirports.tulsa,
            ),
        )

        viewModel.confirmDivert()
        advanceUntilIdle()

        assertEquals(FlightLogStatus.DIVERTED.name, flightLogs.logs.single().status)
        assertEquals("KPWA", flightLogs.logs.single().destinationCode)
        assertEquals("KPWA", settings.currentAirportCode)
    }

    @Test
    fun divertedFlightOnlyPersistsOnceWhenConfirmRepeats() = runTest(dispatcher) {
        val clock = MutableClock(start.plusSeconds(15 * 60))
        val flightLogs = FakeFlightLogRepository()
        val points = FakePointsRepository()
        val settings = FakeSettingsRepository()
        val viewModel = CockpitViewModel(activeFlight, clock, ZoneId.of("UTC"), flightLogs, points, settings.repository)

        viewModel.confirmDivert()
        viewModel.confirmDivert()
        advanceUntilIdle()

        assertEquals(1, flightLogs.logs.size)
        assertEquals(1, points.transactions.size)
        assertEquals("KOUN", settings.currentAirportCode)
    }

    private val start = Instant.parse("2026-05-19T20:00:00Z")
    private val activeFlight = ActiveFlight(
        origin = TestAirports.norman,
        destination = TestAirports.oklahomaCity,
        durationMinutes = 30,
        distanceMiles = 60.0,
        seat = SeatOption("18A", "18A", TicketClass.Standard, 0, "Standard aisle"),
        flightNumber = "TB123",
        startedAt = start,
        plannedArrivalAt = start.plusSeconds(30 * 60),
    )

    private class FakeFlightLogRepository : FlightLogRepository {
        val logs = mutableListOf<FlightLogEntity>()
        private val flow = MutableStateFlow<List<FlightLogEntity>>(emptyList())

        override fun observeFlightLogs(): Flow<List<FlightLogEntity>> = flow

        override suspend fun getFlightLogs(): List<FlightLogEntity> = logs

        override suspend fun getFlightById(id: String): FlightLogEntity? = logs.firstOrNull { it.id == id }

        override suspend fun insertFlightLog(flightLog: FlightLogEntity) {
            logs += flightLog
            flow.value = logs
        }
    }

    private class FakePointsRepository : PointsRepository {
        val transactions = mutableListOf<PointsTransactionEntity>()
        private val transactionFlow = MutableStateFlow<List<PointsTransactionEntity>>(emptyList())
        private val balanceFlow = MutableStateFlow(0)

        override fun observePointsBalance(): Flow<Int> = balanceFlow

        override fun observeTransactions(): Flow<List<PointsTransactionEntity>> = transactionFlow

        override suspend fun getPointsBalance(): Int = balanceFlow.value

        override suspend fun insertTransaction(transaction: PointsTransactionEntity) {
            transactions += transaction
            transactionFlow.value = transactions
            balanceFlow.value = transactions.sumOf { it.amount }
        }
    }

    private class FakeActiveFlightSessionRepository : ActiveFlightSessionRepository {
        var flight: ActiveFlight? = null
        override suspend fun getActiveFlight(): ActiveFlight? = flight
        override suspend fun saveActiveFlight(activeFlight: ActiveFlight) { flight = activeFlight }
        override suspend fun clearActiveFlight() { flight = null }
    }

    private class MutableClock(
        var current: Instant,
        private val zone: ZoneId = ZoneId.of("UTC"),
    ) : Clock() {
        override fun getZone(): ZoneId = zone
        override fun withZone(zone: ZoneId): Clock = MutableClock(current, zone)
        override fun instant(): Instant = current
    }

    private object TestAirports {
        val norman = Airport("KOUN", "KOUN", "OUN", "Norman Airport", "Norman", "Oklahoma", "US", 35.0, -97.0, 1_000)
        val oklahomaCity = Airport("KOKC", "KOKC", "OKC", "Oklahoma City Airport", "Oklahoma City", "Oklahoma", "US", 35.4, -97.6, 1_000)
        val wileyPost = Airport("KPWA", "KPWA", "PWA", "Wiley Post Airport", "Wiley Post", "Oklahoma", "US", 35.2, -97.3, 1_000)
        val tulsa = Airport("KTUL", "KTUL", "TUL", "Tulsa Airport", "Tulsa", "Oklahoma", "US", 36.2, -95.9, 1_000)
    }
}
