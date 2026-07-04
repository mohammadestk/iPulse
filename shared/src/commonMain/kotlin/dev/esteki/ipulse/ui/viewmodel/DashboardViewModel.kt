package dev.esteki.ipulse.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.esteki.ipulse.domain.usecase.*
import dev.esteki.ipulse.ui.model.toConnectionEventUi
import dev.esteki.ipulse.ui.model.toConnectionStateUi
import dev.esteki.ipulse.ui.model.toDeviceUi
import dev.esteki.ipulse.ui.model.toSignalQualityUi
import dev.esteki.ipulse.ui.screen.DashboardAction
import dev.esteki.ipulse.ui.screen.DashboardEvent
import dev.esteki.ipulse.ui.screen.DashboardState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val connectToBroker: ConnectToBrokerUseCase,
    private val disconnectFromBroker: DisconnectFromBrokerUseCase,
    private val observeTelemetryUseCase: ObserveTelemetryUseCase,
    private val observeConnectionStateUseCase: ObserveConnectionStateUseCase,
    private val observeConnectionEventsUseCase: ObserveConnectionEventsUseCase,
    private val observeSignalQualityUseCase: ObserveSignalQualityUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state = _state.asStateFlow()

    private val _events = Channel<DashboardEvent>()
    val events = _events.receiveAsFlow()

    init {
        observeConnectionState()
        observeTelemetryDevices()
        observeConnectionEvents()
        observeSignalQuality()
    }

    fun onAction(action: DashboardAction) {
        when (action) {
            is DashboardAction.OnConnectClick -> connect()
            is DashboardAction.OnDisconnectClick -> disconnect()
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

    private fun connect() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                connectToBroker()
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _events.send(DashboardEvent.ShowError(e.message ?: "Connection failed"))
            }
        }
    }

    private fun disconnect() {
        viewModelScope.launch {
            try {
                disconnectFromBroker()
            } catch (e: Exception) {
                _events.send(DashboardEvent.ShowError(e.message ?: "Disconnection failed"))
            }
        }
    }

    private fun observeConnectionState() {
        viewModelScope.launch {
            observeConnectionStateUseCase().collect { connectionState ->
                _state.update { it.copy(connectionState = connectionState.toConnectionStateUi()) }
            }
        }
    }

    private fun observeTelemetryDevices() {
        viewModelScope.launch {
            observeTelemetryUseCase().collect { devices ->
                _state.update { it.copy(devices = devices.map { device -> device.toDeviceUi() }) }
            }
        }
    }

    private fun observeConnectionEvents() {
        viewModelScope.launch {
            observeConnectionEventsUseCase().collect { event ->
                _state.update { state ->
                    val events = (listOf(event.toConnectionEventUi()) + state.connectionEvents).take(50)
                    state.copy(connectionEvents = events)
                }
            }
        }
    }

    private fun observeSignalQuality() {
        viewModelScope.launch {
            observeSignalQualityUseCase().collect { quality ->
                _state.update { it.copy(signalQuality = quality.toSignalQualityUi()) }
            }
        }
    }
}
