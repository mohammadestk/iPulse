package dev.esteki.ipulse.presentation.screen

import dev.esteki.ipulse.domain.model.ConnectionState
import dev.esteki.ipulse.presentation.model.ConnectionEventUi
import dev.esteki.ipulse.presentation.model.ConnectionStateUi
import dev.esteki.ipulse.presentation.model.DeviceUi
import dev.esteki.ipulse.presentation.model.SignalQualityUi

data class DashboardState(
    val devices: List<DeviceUi> = emptyList(),
    val connectionState: ConnectionStateUi = ConnectionStateUi(
        state = ConnectionState.Disconnected,
        displayName = "Disconnected",
        isConnected = false
    ),
    val signalQuality: SignalQualityUi? = null,
    val connectionEvents: List<ConnectionEventUi> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val errorMessage: String? = null
) {
    val filteredDevices: List<DeviceUi>
        get() = if (searchQuery.isBlank()) devices
        else devices.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.topic.contains(searchQuery, ignoreCase = true)
        }
}
