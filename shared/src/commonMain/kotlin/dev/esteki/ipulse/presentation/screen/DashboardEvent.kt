package dev.esteki.ipulse.presentation.screen

sealed interface DashboardEvent {
    data class NavigateToDeviceDetail(val deviceId: String) : DashboardEvent
    data class ShowError(val message: String) : DashboardEvent
}
