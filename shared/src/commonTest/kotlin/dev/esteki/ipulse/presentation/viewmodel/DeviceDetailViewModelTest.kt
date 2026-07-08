package dev.esteki.ipulse.presentation.viewmodel

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.esteki.ipulse.domain.model.ConnectionEvent
import dev.esteki.ipulse.domain.model.ConnectionState
import dev.esteki.ipulse.domain.model.SensorType
import dev.esteki.ipulse.domain.model.SignalQuality
import dev.esteki.ipulse.domain.model.Stability
import dev.esteki.ipulse.domain.model.TelemetryReading
import dev.esteki.ipulse.domain.repository.BrokerConnection
import dev.esteki.ipulse.domain.repository.DeviceRepository
import dev.esteki.ipulse.domain.usecase.GetDeviceReadings
import dev.esteki.ipulse.domain.usecase.ObserveConnectionEvents
import dev.esteki.ipulse.domain.usecase.ObserveDeviceById
import dev.esteki.ipulse.domain.usecase.ObserveSignalQuality
import dev.esteki.ipulse.presentation.screen.DeviceDetailState
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
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

    private val repository = mockk<DeviceRepository>()
    private val broker = mockk<BrokerConnection>()

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
        every { repository.observeReadingsForDevice(any()) } returns emptyFlow()
        every { repository.observeDeviceById(any()) } returns flowOf(null)
        every { repository.signalQuality } returns flowOf(SignalQuality(0.0, Stability.NO_DATA))
        every { broker.connectionEvents } returns emptyFlow()
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

        assertThat(vm.state.value.readings).isEmpty()
    }

    @Test
    fun readingsAppearWhenEmitted() = runTest {
        every { repository.observeReadingsForDevice(deviceId) } returns flowOf(listOf(reading(24.6)))
        val vm = createViewModel()

        vm.state.test {
            val state = awaitItem()
            assertThat(state.readings).hasSize(1)
            assertThat(state.readings[0].value).isEqualTo(24.6)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun readingsAccumulate() = runTest {
        val readings = listOf(reading(10.0), reading(20.0), reading(30.0))
        every { repository.observeReadingsForDevice(deviceId) } returns flowOf(readings)
        val vm = createViewModel()

        vm.state.test {
            val state = awaitItem()
            assertThat(state.readings).hasSize(3)
            assertThat(state.readings[2].value).isEqualTo(30.0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun readingsCappedAt100() = runTest {
        val manyReadings = (1..150).map { reading(it.toDouble()) }
        every { repository.observeReadingsForDevice(deviceId) } returns flowOf(manyReadings)
        val vm = createViewModel()

        vm.state.test {
            val state = awaitItem()
            assertThat(state.readings).hasSize(100)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
