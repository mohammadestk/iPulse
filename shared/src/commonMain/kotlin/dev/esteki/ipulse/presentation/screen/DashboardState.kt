package dev.esteki.ipulse.presentation.screen

import androidx.compose.runtime.Stable
import dev.esteki.ipulse.presentation.model.ConnectionEventUi
import dev.esteki.ipulse.presentation.model.ConnectionStateUi
import dev.esteki.ipulse.presentation.model.DeviceUi
import dev.esteki.ipulse.presentation.model.SignalQualityUi

@Stable
data class DashboardState(
    val devices: List<DeviceUi>,
    val connectionState: ConnectionStateUi,
    val signalQuality: SignalQualityUi?,
    val connectionEvents: List<ConnectionEventUi>,
    val isLoading: Boolean,
    val searchQuery: String,
    val errorMessage: String?
) {
    val filteredDevices: List<DeviceUi>
        get() = if (searchQuery.isBlank()) devices
        else devices.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.topic.contains(searchQuery, ignoreCase = true)
        }
}
