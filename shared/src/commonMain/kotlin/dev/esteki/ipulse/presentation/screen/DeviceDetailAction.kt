package dev.esteki.ipulse.presentation.screen

sealed interface DeviceDetailAction {
    data object OnBackClick : DeviceDetailAction
    data object OnRefreshClick : DeviceDetailAction
    data object OnErrorDismissed : DeviceDetailAction
}
