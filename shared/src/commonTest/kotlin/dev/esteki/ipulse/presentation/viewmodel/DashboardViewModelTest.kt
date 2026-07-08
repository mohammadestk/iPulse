package dev.esteki.ipulse.presentation.viewmodel

import app.cash.turbine.test
import dev.esteki.ipulse.domain.model.ConnectionState
import dev.esteki.ipulse.domain.model.Device
import dev.esteki.ipulse.domain.model.SensorType
import dev.esteki.ipulse.domain.repository.FakeBrokerConnection
import dev.esteki.ipulse.domain.repository.FakeDeviceRepository
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
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.Test
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
class DashboardViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val repository = FakeDeviceRepository()
    private val broker = FakeBrokerConnection()

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

        assertEquals(1, broker.connectCalls.size)
        assertEquals(1, broker.subscribeCalls.size)
        assertEquals("/esteki/devices", broker.subscribeCalls[0])
    }

    @Test
    fun onAction_searchQuery_updatesState() = runTest {
        val vm = createViewModel()

        vm.onAction(DashboardAction.OnSearchQueryChange("ward"))

        assertEquals("ward", vm.state.value.searchQuery)
    }

    @Test
    fun onAction_errorDismissed_clearsError() = runTest {
        val vm = createViewModel()

        vm.onAction(DashboardAction.OnErrorDismissed)

        assertNull(vm.state.value.errorMessage)
    }

    @Test
    fun connectionState_updatesUi() = runTest {
        broker.setConnectionState(ConnectionState.Connected)
        val vm = createViewModel()

        vm.state.test {
            val state = awaitItem()
            assertTrue(state.connectionState.isConnected)
            assertEquals("Connected", state.connectionState.displayName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun connectionState_reconnecting_updatesUi() = runTest {
        broker.setConnectionState(ConnectionState.Reconnecting)
        val vm = createViewModel()

        vm.state.test {
            val state = awaitItem()
            assertEquals("Reconnecting", state.connectionState.displayName)
            assertTrue(!state.connectionState.isConnected)
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
        repository.setDevices(listOf(device))
        val vm = createViewModel()

        vm.state.test {
            val state = awaitItem()
            assertEquals(1, state.devices.size)
            assertEquals("device-1", state.devices[0].id)
            assertEquals("Ward B", state.devices[0].name)
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
        repository.setDevices(devices)
        val vm = createViewModel()

        vm.onAction(DashboardAction.OnSearchQueryChange("ward"))

        assertEquals(2, vm.state.value.filteredDevices.size)
    }

    @Test
    fun filteredDevices_emptyQueryShowsAll() = runTest {
        val devices = listOf(
            Device("d1", "Ward A", "/t", SensorType.TEMPERATURE, null, ConnectionState.Connected),
            Device("d2", "Storage", "/t", SensorType.HUMIDITY, null, ConnectionState.Connected)
        )
        repository.setDevices(devices)
        val vm = createViewModel()

        vm.onAction(DashboardAction.OnSearchQueryChange(""))

        assertEquals(2, vm.state.value.filteredDevices.size)
    }
}
