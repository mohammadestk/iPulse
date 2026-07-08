package dev.esteki.ipulse.presentation.screen

import dev.esteki.ipulse.presentation.model.ConnectionEventUi
import dev.esteki.ipulse.presentation.model.DeviceUi
import dev.esteki.ipulse.presentation.model.SignalQualityUi

data class DeviceDetailState(
    val device: DeviceUi? = null,
    val signalQuality: SignalQualityUi? = null,
    val connectionEvents: List<ConnectionEventUi> = emptyList(),
    val readings: List<ReadingUi> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
