package com.travelblock.app.ui.screen.cockpit

import com.travelblock.app.data.database.FlightLogEntity
import com.travelblock.app.data.database.FlightLogStatus
import com.travelblock.app.data.database.PointsTransactionEntity
import com.travelblock.app.data.repository.FlightLogRepository
import com.travelblock.app.data.repository.PointsRepository
import com.travelblock.app.data.settings.SettingsRepository
import com.travelblock.app.data.settings.UserSettings
import com.travelblock.app.domain.model.ActiveFlight
import com.travelblock.app.domain.model.Airport
import com.travelblock.app.domain.model.SeatOption
import com.travelblock.app.domain.model.TicketClass
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
        val viewModel = CockpitViewModel(activeFlight, clock, ZoneId.of("UTC"), flightLogs, points, settings)

        viewModel.tick()
        advanceUntilIdle()

        assertEquals(1, flightLogs.logs.size)
        assertEquals(FlightLogStatus.COMPLETED.name, flightLogs.logs.single().status)
        assertEquals(151, flightLogs.logs.single().pointsEarned)
        assertEquals(1, points.transactions.size)
        assertEquals(151, points.transactions.single().amount)
        assertEquals("KOKC", settings.currentAirportCode)
    }

    @Test
    fun completedFlightOnlyPersistsOnceWhenTickRepeats() = runTest(dispatcher) {
        val clock = MutableClock(start.plusSeconds(31 * 60))
        val flightLogs = FakeFlightLogRepository()
        val points = FakePointsRepository()
        val settings = FakeSettingsRepository()
        val viewModel = CockpitViewModel(activeFlight, clock, ZoneId.of("UTC"), flightLogs, points, settings)

        viewModel.tick()
        viewModel.tick()
        advanceUntilIdle()

        assertEquals(1, flightLogs.logs.size)
        assertEquals(1, points.transactions.size)
        assertEquals("KOKC", settings.currentAirportCode)
    }

    @Test
    fun divertedFlightInsertsDivertedLogWithoutMovingCurrentAirport() = runTest(dispatcher) {
        val clock = MutableClock(start.plusSeconds(5 * 60))
        val flightLogs = FakeFlightLogRepository()
        val points = FakePointsRepository()
        val settings = FakeSettingsRepository()
        val viewModel = CockpitViewModel(activeFlight, clock, ZoneId.of("UTC"), flightLogs, points, settings)

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
        val viewModel = CockpitViewModel(activeFlight, clock, ZoneId.of("UTC"), flightLogs, points, settings)

        viewModel.confirmDivert()
        advanceUntilIdle()

        assertEquals(1, flightLogs.logs.size)
        assertEquals(FlightLogStatus.DIVERTED.name, flightLogs.logs.single().status)
        assertEquals(15, flightLogs.logs.single().durationMinutes)
        assertEquals(37, flightLogs.logs.single().pointsEarned)
        assertEquals(37, points.transactions.single().amount)
        assertEquals("KOUN", settings.currentAirportCode)
    }

    @Test
    fun divertedFlightMovesCurrentAirportToNearestDiversionAirport() = runTest(dispatcher) {
        val clock = MutableClock(start.plusSeconds(15 * 60))
        val flightLogs = FakeFlightLogRepository()
        val points = FakePointsRepository()
        val settings = FakeSettingsRepository()
        val diversionAirport = airport("KPWA", "Wiley Post", latitude = 35.2, longitude = -97.3)
        val viewModel = CockpitViewModel(
            activeFlight = activeFlight,
            clock = clock,
            zoneId = ZoneId.of("UTC"),
            flightLogRepository = flightLogs,
            pointsRepository = points,
            settingsRepository = settings,
            airports = listOf(
                activeFlight.origin,
                activeFlight.destination,
                diversionAirport,
                airport("KTUL", "Tulsa", latitude = 36.2, longitude = -95.9),
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
        val viewModel = CockpitViewModel(activeFlight, clock, ZoneId.of("UTC"), flightLogs, points, settings)

        viewModel.confirmDivert()
        viewModel.confirmDivert()
        advanceUntilIdle()

        assertEquals(1, flightLogs.logs.size)
        assertEquals(1, points.transactions.size)
        assertEquals("KOUN", settings.currentAirportCode)
    }

    private val start = Instant.parse("2026-05-19T20:00:00Z")
    private val activeFlight = ActiveFlight(
        origin = airport("KOUN", "Norman"),
        destination = airport("KOKC", "Oklahoma City", latitude = 35.4, longitude = -97.6),
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

    private class FakeSettingsRepository : SettingsRepository {
        var currentAirportCode: String = "KOUN"
        private val state = MutableStateFlow(UserSettings())

        override val settings: Flow<UserSettings> = state

        override suspend fun getSettings(): UserSettings = state.value

        override suspend fun setHomeAirportCode(code: String) {
            state.value = state.value.copy(homeAirportCode = code)
        }

        override suspend fun setCurrentAirportCode(code: String) {
            currentAirportCode = code
            state.value = state.value.copy(currentAirportCode = code)
        }

        override suspend fun setSelectedTheme(theme: String) {
            state.value = state.value.copy(selectedTheme = theme)
        }

        override suspend fun setSoundEnabled(enabled: Boolean) {
            state.value = state.value.copy(soundEnabled = enabled)
        }

        override suspend fun setHapticsEnabled(enabled: Boolean) {
            state.value = state.value.copy(hapticsEnabled = enabled)
        }

        override suspend fun setEquippedBoardingPassThemeId(id: String) {
            state.value = state.value.copy(equippedBoardingPassThemeId = id)
        }

        override suspend fun setEquippedAircraftIconId(id: String) {
            state.value = state.value.copy(equippedAircraftIconId = id)
        }

        override suspend fun resetToDefaults() {
            currentAirportCode = UserSettings.DefaultAirportCode
            state.value = UserSettings()
        }
    }

    private class MutableClock(
        var current: Instant,
        private val zone: ZoneId = ZoneId.of("UTC"),
    ) : Clock() {
        override fun getZone(): ZoneId = zone
        override fun withZone(zone: ZoneId): Clock = MutableClock(current, zone)
        override fun instant(): Instant = current
    }

    private fun airport(
        code: String,
        city: String,
        latitude: Double = 35.0,
        longitude: Double = -97.0,
    ): Airport {
        return Airport(
            code = code,
            icao = code,
            iata = code.takeLast(3),
            name = "$city Airport",
            city = city,
            stateOrRegion = "Oklahoma",
            country = "US",
            latitude = latitude,
            longitude = longitude,
            elevationFeet = 1_000,
        )
    }
}
