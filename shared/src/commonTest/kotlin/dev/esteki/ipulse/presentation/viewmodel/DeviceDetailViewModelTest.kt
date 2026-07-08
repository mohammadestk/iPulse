package dev.esteki.ipulse.presentation.viewmodel

import app.cash.turbine.test
import dev.esteki.ipulse.domain.model.ConnectionState
import dev.esteki.ipulse.domain.model.SensorType
import dev.esteki.ipulse.domain.model.SignalQuality
import dev.esteki.ipulse.domain.model.Stability
import dev.esteki.ipulse.domain.model.TelemetryReading
import dev.esteki.ipulse.domain.repository.FakeBrokerConnection
import dev.esteki.ipulse.domain.repository.FakeDeviceRepository
import dev.esteki.ipulse.domain.usecase.GetDeviceReadings
import dev.esteki.ipulse.domain.usecase.ObserveConnectionEvents
import dev.esteki.ipulse.domain.usecase.ObserveDeviceById
import dev.esteki.ipulse.domain.usecase.ObserveSignalQuality
import dev.esteki.ipulse.presentation.screen.DeviceDetailState
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

@OptIn(ExperimentalCoroutinesApi::class)
class DeviceDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val deviceId = "device-1"

    private val repository = FakeDeviceRepository()
    private val broker = FakeBrokerConnection()

    private val defaultState = DeviceDetailState(
        device = null,
        signalQuality = null,
        connectionEvents = emptyList(),
        readings = emptyList(),
        isLoading = true,
        errorMessage = null
    )

    private fun reading(value: Double) = TelemetryReading(
        id = 1L,
        value = value,
        sensorType = SensorType.TEMPERATURE,
        timestamp = Instant.fromEpochMilliseconds(1_720_000_000_000),
        topic = "/esteki/devices"
    )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository.setSignalQuality(SignalQuality(0.0, Stability.NO_DATA))
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = DeviceDetailViewModel(
        deviceDetailState = defaultState,
        deviceId = deviceId,
        observeDeviceById = ObserveDeviceById(repository),
        observeConnectionEvents = ObserveConnectionEvents(broker),
        observeSignalQuality = ObserveSignalQuality(repository),
        getDeviceReadings = GetDeviceReadings(repository)
    )

    @Test
    fun initialState_hasEmptyReadings() = runTest {
        val vm = createViewModel()

        assertTrue(vm.state.value.readings.isEmpty())
    }

    @Test
    fun readingsAppearWhenEmitted() = runTest {
        repository.setReadings(deviceId, listOf(reading(24.6)))
        val vm = createViewModel()

        vm.state.test {
            val state = awaitItem()
            assertEquals(1, state.readings.size)
            assertEquals(24.6, state.readings[0].value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun readingsAccumulate() = runTest {
        val readings = listOf(reading(10.0), reading(20.0), reading(30.0))
        repository.setReadings(deviceId, readings)
        val vm = createViewModel()

        vm.state.test {
            val state = awaitItem()
            assertEquals(3, state.readings.size)
            assertEquals(30.0, state.readings[2].value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun readingsCappedAt100() = runTest {
        val manyReadings = (1..150).map { reading(it.toDouble()) }
        repository.setReadings(deviceId, manyReadings)
        val vm = createViewModel()

        vm.state.test {
            val state = awaitItem()
            assertEquals(100, state.readings.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
