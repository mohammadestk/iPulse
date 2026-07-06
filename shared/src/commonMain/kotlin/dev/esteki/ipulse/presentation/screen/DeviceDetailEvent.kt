package dev.esteki.ipulse.presentation.screen

sealed interface DeviceDetailEvent {
    data object NavigateBack : DeviceDetailEvent
    data class ShowError(val message: String) : DeviceDetailEvent
}
