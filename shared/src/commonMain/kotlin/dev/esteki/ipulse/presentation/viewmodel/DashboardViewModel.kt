package dev.esteki.ipulse.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.esteki.ipulse.domain.usecase.*
import dev.esteki.ipulse.presentation.model.toConnectionEventUi
import dev.esteki.ipulse.presentation.model.toConnectionStateUi
import dev.esteki.ipulse.presentation.model.toDeviceUi
import dev.esteki.ipulse.presentation.model.toSignalQualityUi
import dev.esteki.ipulse.presentation.screen.DashboardAction
import dev.esteki.ipulse.presentation.screen.DashboardEvent
import dev.esteki.ipulse.presentation.screen.DashboardState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val connectToBroker: ConnectToBroker,
    private val subscribeToDeviceTopic: SubscribeToDeviceTopic,
    private val observeTelemetry: ObserveTelemetry,
    private val observeConnectionState: ObserveConnectionState,
    private val observeConnectionEvents: ObserveConnectionEvents,
    private val observeSignalQuality: ObserveSignalQuality
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state = _state.asStateFlow()

    private val _events = Channel<DashboardEvent>()
    val events = _events.receiveAsFlow()

    init {
        collectConnectionState()
        collectTelemetryDevices()
        collectConnectionEvents()
        collectSignalQuality()
        autoConnect()
    }

    private fun autoConnect() {
        viewModelScope.launch {
            connectToBroker("test.mosquitto.org", 8081)
                .onSuccess { subscribeToDeviceTopic("#") }
                .onFailure { error ->
                    _state.update { it.copy(errorMessage = error.message ?: "Connection failed") }
                    _events.send(DashboardEvent.ShowError(error.message ?: "Connection failed"))
                }
        }
    }

    fun onAction(action: DashboardAction) {
        when (action) {
            is DashboardAction.OnSearchQueryChange -> {
                _state.update { it.copy(searchQuery = action.query) }
            }
            is DashboardAction.OnDeviceClick -> {
                viewModelScope.launch {
                    _events.send(DashboardEvent.NavigateToDeviceDetail(action.deviceId))
                }
            }
            is DashboardAction.OnErrorDismissed -> {
                _state.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun collectConnectionState() {
        observeConnectionState()
            .onEach { connectionState ->
                _state.update { it.copy(connectionState = connectionState.toConnectionStateUi()) }
            }
            .catch { _state.update { it.copy(errorMessage = "Connection state monitor failed") } }
            .launchIn(viewModelScope)
    }

    private fun collectTelemetryDevices() {
        observeTelemetry()
            .onEach { devices ->
                _state.update { it.copy(devices = devices.map { device -> device.toDeviceUi() }) }
            }
            .catch { _state.update { it.copy(errorMessage = "Device list monitor failed") } }
            .launchIn(viewModelScope)
    }

    private fun collectConnectionEvents() {
        observeConnectionEvents()
            .onEach { event ->
                _state.update { state ->
                    val events = (listOf(event.toConnectionEventUi()) + state.connectionEvents).take(50)
                    state.copy(connectionEvents = events)
                }
            }
            .catch { /* Connection events monitor failed */ }
            .launchIn(viewModelScope)
    }

    private fun collectSignalQuality() {
        observeSignalQuality()
            .onEach { quality ->
                _state.update { it.copy(signalQuality = quality.toSignalQualityUi()) }
            }
            .catch { /* Signal quality monitor failed */ }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        println("Home viewmodel cleared")
    }
}
