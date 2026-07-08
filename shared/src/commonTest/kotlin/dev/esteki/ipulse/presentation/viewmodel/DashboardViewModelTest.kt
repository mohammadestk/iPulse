package dev.esteki.ipulse.presentation.viewmodel

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.esteki.ipulse.domain.model.ConnectionState
import dev.esteki.ipulse.domain.model.Device
import dev.esteki.ipulse.domain.model.SensorType
import dev.esteki.ipulse.domain.repository.BrokerConnection
import dev.esteki.ipulse.domain.repository.DeviceRepository
import dev.esteki.ipulse.domain.usecase.ConnectToBroker
import dev.esteki.ipulse.domain.usecase.ObserveConnectionEvents
import dev.esteki.ipulse.domain.usecase.ObserveConnectionState
import dev.esteki.ipulse.domain.usecase.ObserveDevicesPaged
import dev.esteki.ipulse.domain.usecase.ObserveSignalQuality
import dev.esteki.ipulse.domain.usecase.ObserveTelemetry
import dev.esteki.ipulse.domain.usecase.SubscribeToDeviceTopic
import dev.esteki.ipulse.presentation.screen.DashboardAction
import dev.esteki.ipulse.presentation.screen.DashboardState
import dev.esteki.ipulse.presentation.model.ConnectionStateUi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
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
class DashboardViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository = mockk<DeviceRepository>()
    private val broker = mockk<BrokerConnection>()

    private val defaultState = DashboardState(
        devices = emptyList(),
        connectionState = ConnectionStateUi(
            state = ConnectionState.Disconnected,
            displayName = "Disconnected",
            isConnected = false
        ),
        signalQuality = null,
        connectionEvents = emptyList(),
        isLoading = false,
        searchQuery = "",
        errorMessage = null
    )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.devices } returns emptyFlow()
        every { repository.signalQuality } returns emptyFlow()
        every { repository.observeDevicesPaged() } returns emptyFlow()
        every { broker.connectionState } returns emptyFlow()
        every { broker.connectionEvents } returns emptyFlow()
        coEvery { broker.connect(any(), any()) } returns Unit
        coEvery { broker.subscribe(any()) } returns Unit
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = DashboardViewModel(
        dashboardState = defaultState,
        connectToBroker = ConnectToBroker(broker),
        subscribeToDeviceTopic = SubscribeToDeviceTopic(broker),
        observeTelemetry = ObserveTelemetry(repository),
        observeDevicesPaged = ObserveDevicesPaged(repository),
        observeConnectionState = ObserveConnectionState(broker),
        observeConnectionEvents = ObserveConnectionEvents(broker),
        observeSignalQuality = ObserveSignalQuality(repository)
    )

    @Test
    fun autoConnect_connectsAndSubscribes() = runTest {
        createViewModel()

        coVerify { broker.connect(any(), any()) }
        coVerify { broker.subscribe("/esteki/devices") }
    }

    @Test
    fun onAction_searchQuery_updatesState() = runTest {
        val vm = createViewModel()

        vm.onAction(DashboardAction.OnSearchQueryChange("ward"))

        assertThat(vm.state.value.searchQuery).isEqualTo("ward")
    }

    @Test
    fun onAction_errorDismissed_clearsError() = runTest {
        val vm = createViewModel()

        vm.onAction(DashboardAction.OnErrorDismissed)

        assertThat(vm.state.value.errorMessage).isNull()
    }

    @Test
    fun connectionState_updatesUi() = runTest {
        every { broker.connectionState } returns flowOf(ConnectionState.Connected)
        val vm = createViewModel()

        vm.state.test {
            val state = awaitItem()
            assertThat(state.connectionState.isConnected).isTrue()
            assertThat(state.connectionState.displayName).isEqualTo("Connected")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun connectionState_reconnecting_updatesUi() = runTest {
        every { broker.connectionState } returns flowOf(ConnectionState.Reconnecting)
        val vm = createViewModel()

        vm.state.test {
            val state = awaitItem()
            assertThat(state.connectionState.displayName).isEqualTo("Reconnecting")
            assertThat(state.connectionState.isConnected).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun telemetryDevices_updatesDeviceList() = runTest {
        val device = Device(
            id = "device-1",
            name = "Ward B",
            topic = "/esteki/devices",
            sensorType = SensorType.TEMPERATURE,
            latestReading = null,
            connectionState = ConnectionState.Connected
        )
        every { repository.devices } returns flowOf(listOf(device))
        val vm = createViewModel()

        vm.state.test {
            val state = awaitItem()
            assertThat(state.devices).hasSize(1)
            assertThat(state.devices[0].id).isEqualTo("device-1")
            assertThat(state.devices[0].name).isEqualTo("Ward B")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun filteredDevices_filtersByName() = runTest {
        val devices = listOf(
            Device("d1", "Ward A", "/t", SensorType.TEMPERATURE, null, ConnectionState.Connected),
            Device("d2", "Ward B", "/t", SensorType.HUMIDITY, null, ConnectionState.Connected),
            Device("d3", "Storage", "/t", SensorType.PRESSURE, null, ConnectionState.Connected)
        )
        every { repository.devices } returns flowOf(devices)
        val vm = createViewModel()

        vm.onAction(DashboardAction.OnSearchQueryChange("ward"))

        assertThat(vm.state.value.filteredDevices).hasSize(2)
    }

    @Test
    fun filteredDevices_emptyQueryShowsAll() = runTest {
        val devices = listOf(
            Device("d1", "Ward A", "/t", SensorType.TEMPERATURE, null, ConnectionState.Connected),
            Device("d2", "Storage", "/t", SensorType.HUMIDITY, null, ConnectionState.Connected)
        )
        every { repository.devices } returns flowOf(devices)
        val vm = createViewModel()

        vm.onAction(DashboardAction.OnSearchQueryChange(""))

        assertThat(vm.state.value.filteredDevices).hasSize(2)
    }
}
