package dev.esteki.ipulse.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.esteki.ipulse.domain.usecase.GetDeviceById
import dev.esteki.ipulse.domain.usecase.ObserveConnectionEvents
import dev.esteki.ipulse.domain.usecase.ObserveSignalQuality
import dev.esteki.ipulse.presentation.model.toConnectionEventUi
import dev.esteki.ipulse.presentation.model.toDeviceUi
import dev.esteki.ipulse.presentation.model.toSignalQualityUi
import dev.esteki.ipulse.presentation.screen.DeviceDetailAction
import dev.esteki.ipulse.presentation.screen.DeviceDetailEvent
import dev.esteki.ipulse.presentation.screen.DeviceDetailState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DeviceDetailViewModel(
    private val deviceId: String,
    private val getDeviceById: GetDeviceById,
    private val observeConnectionEvents: ObserveConnectionEvents,
    private val observeSignalQuality: ObserveSignalQuality
) : ViewModel() {

    private val _state = MutableStateFlow(DeviceDetailState())
    val state = _state.asStateFlow()

    private val _events = Channel<DeviceDetailEvent>()
    val events = _events.receiveAsFlow()

    init {
        println("device detail viewmodel initialized")

        loadDevice()
        collectConnectionEvents()
        collectSignalQuality()
    }

    fun onAction(action: DeviceDetailAction) {
        when (action) {
            is DeviceDetailAction.OnBackClick -> {
                viewModelScope.launch {
                    _events.send(DeviceDetailEvent.NavigateBack)
                }
            }
            is DeviceDetailAction.OnRefreshClick -> loadDevice()
        }
    }

    private fun loadDevice() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val device = getDeviceById(deviceId)
                if (device != null) {
                    val deviceUi = device.toDeviceUi()
                    _state.update {
                        it.copy(
                            device = deviceUi,
                            latestValue = deviceUi.latestValue,
                            unit = deviceUi.unit,
                            sensorType = device.sensorType.displayName,
                            connectionState = device.connectionState,
                            isLoading = false
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(DeviceDetailEvent.ShowError("Device not found"))
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _events.send(DeviceDetailEvent.ShowError(e.message ?: "Failed to load device"))
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

    override fun onCleared() {
        println("device detail viewmodel cleared")
    }
}
