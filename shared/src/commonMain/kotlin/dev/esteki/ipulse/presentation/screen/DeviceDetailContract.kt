package dev.esteki.ipulse.presentation.screen

import dev.esteki.ipulse.domain.model.ConnectionState
import dev.esteki.ipulse.presentation.model.ConnectionEventUi
import dev.esteki.ipulse.presentation.model.DeviceUi
import dev.esteki.ipulse.presentation.model.SignalQualityUi
import kotlin.time.Instant

data class DeviceDetailState(
    val device: DeviceUi? = null,
    val latestValue: String = "--",
    val unit: String = "",
    val sensorType: String = "",
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val signalQuality: SignalQualityUi? = null,
    val connectionEvents: List<ConnectionEventUi> = emptyList(),
    val readings: List<ReadingUi> = emptyList(),
    val isLoading: Boolean = false
)

data class ReadingUi(
    val value: Double,
    val timestamp: Instant,
    val formattedTime: String
)

sealed interface DeviceDetailAction {
    data object OnBackClick : DeviceDetailAction
    data object OnRefreshClick : DeviceDetailAction
}

sealed interface DeviceDetailEvent {
    data object NavigateBack : DeviceDetailEvent
    data class ShowError(val message: String) : DeviceDetailEvent
}
