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
    private val connectToBroker: ConnectToBrokerUseCase,
    private val subscribeToDeviceTopic: SubscribeToDeviceTopicUseCase,
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
        autoConnect()
    }

    private fun autoConnect() {
        viewModelScope.launch {
            try {
                connectToBroker()
                subscribeToDeviceTopic("#")
            } catch (_: Exception) {
                // Connection state will be updated via observeConnectionState
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
