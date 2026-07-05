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
            try {
                connectToBroker("test.mosquitto.org", 8081)
                subscribeToDeviceTopic("#")
            } catch (_: Exception) {
                // Connection state will be updated via collectConnectionState
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
        }
    }

    private fun collectConnectionState() {
        viewModelScope.launch {
            observeConnectionState().collect { connectionState ->
                _state.update { it.copy(connectionState = connectionState.toConnectionStateUi()) }
            }
        }
    }

    private fun collectTelemetryDevices() {
        viewModelScope.launch {
            observeTelemetry().collect { devices ->
                _state.update { it.copy(devices = devices.map { device -> device.toDeviceUi() }) }
            }
        }
    }

    private fun collectConnectionEvents() {
        viewModelScope.launch {
            observeConnectionEvents().collect { event ->
                _state.update { state ->
                    val events = (listOf(event.toConnectionEventUi()) + state.connectionEvents).take(50)
                    state.copy(connectionEvents = events)
                }
            }
        }
    }

    private fun collectSignalQuality() {
        viewModelScope.launch {
            observeSignalQuality().collect { quality ->
                _state.update { it.copy(signalQuality = quality.toSignalQualityUi()) }
            }
        }
    }
}
