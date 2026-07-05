package dev.esteki.ipulse.ui.screen

import dev.esteki.ipulse.domain.model.ConnectionState
import dev.esteki.ipulse.ui.model.ConnectionEventUi
import dev.esteki.ipulse.ui.model.ConnectionStateUi
import dev.esteki.ipulse.ui.model.DeviceUi
import dev.esteki.ipulse.ui.model.SignalQualityUi

data class DashboardState(
    val devices: List<DeviceUi> = emptyList(),
    val connectionState: ConnectionStateUi = ConnectionStateUi(
        state = ConnectionState.DISCONNECTED,
        displayName = "Disconnected",
        isConnected = false
    ),
    val signalQuality: SignalQualityUi? = null,
    val connectionEvents: List<ConnectionEventUi> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = ""
) {
    val filteredDevices: List<DeviceUi>
        get() = if (searchQuery.isBlank()) devices
        else devices.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.topic.contains(searchQuery, ignoreCase = true)
        }
}

sealed interface DashboardAction {
    data class OnSearchQueryChange(val query: String) : DashboardAction
    data class OnDeviceClick(val deviceId: String) : DashboardAction
}

sealed interface DashboardEvent {
    data class NavigateToDeviceDetail(val deviceId: String) : DashboardEvent
    data class ShowError(val message: String) : DashboardEvent
}
