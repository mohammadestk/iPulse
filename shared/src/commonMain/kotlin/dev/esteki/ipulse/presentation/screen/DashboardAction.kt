package dev.esteki.ipulse.presentation.screen

sealed interface DashboardAction {
    data class OnSearchQueryChange(val query: String) : DashboardAction
    data class OnDeviceClick(val deviceId: String) : DashboardAction
}
