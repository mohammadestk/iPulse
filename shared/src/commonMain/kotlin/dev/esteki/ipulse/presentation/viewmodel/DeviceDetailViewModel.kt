package dev.esteki.ipulse.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.esteki.ipulse.domain.usecase.GetDeviceByIdUseCase
import dev.esteki.ipulse.domain.usecase.ObserveConnectionEventsUseCase
import dev.esteki.ipulse.domain.usecase.ObserveSignalQualityUseCase
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
    private val getDeviceById: GetDeviceByIdUseCase,
    private val observeConnectionEventsUseCase: ObserveConnectionEventsUseCase,
    private val observeSignalQualityUseCase: ObserveSignalQualityUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DeviceDetailState())
    val state = _state.asStateFlow()

    private val _events = Channel<DeviceDetailEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadDevice()
        observeConnectionEvents()
        observeSignalQuality()
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
