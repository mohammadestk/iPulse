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
            is DeviceDetailAction.OnRefreshClick -> {
                _state.update { it.copy(errorMessage = null) }
                loadDevice()
            }
            is DeviceDetailAction.OnErrorDismissed -> {
                _state.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun loadDevice() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getDeviceById(deviceId)
                .onSuccess { device ->
                    _state.update {
                        it.copy(
                            device = device.toDeviceUi(),
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load device"
                        )
                    }
                    _events.send(DeviceDetailEvent.ShowError(error.message ?: "Failed to load device"))
                }
        }
    }

    private fun collectConnectionEvents() {
        observeConnectionEvents()
            .onEach { event ->
                _state.update { state ->
                    val events =
                        (listOf(event.toConnectionEventUi()) + state.connectionEvents).take(50)
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

}
