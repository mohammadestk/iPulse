package dev.esteki.ipulse.presentation.screen

import androidx.compose.runtime.Stable
import dev.esteki.ipulse.presentation.model.ConnectionEventUi
import dev.esteki.ipulse.presentation.model.DeviceUi
import dev.esteki.ipulse.presentation.model.SignalQualityUi

@Stable
data class DeviceDetailState(
    val device: DeviceUi?,
    val signalQuality: SignalQualityUi?,
    val connectionEvents: List<ConnectionEventUi>,
    val readings: List<ReadingUi>,
    val isLoading: Boolean,
    val errorMessage: String?
)
