package dev.esteki.ipulse.presentation.model

import dev.esteki.ipulse.domain.model.ConnectionState

data class ConnectionStateUi(
    val state: ConnectionState,
    val displayName: String,
    val isConnected: Boolean
)

fun ConnectionState.toConnectionStateUi(): ConnectionStateUi = ConnectionStateUi(
    state = this,
    displayName = when (this) {
        is ConnectionState.Connected -> "Connected"
        is ConnectionState.Connecting -> "Connecting"
        is ConnectionState.Reconnecting -> "Reconnecting"
        is ConnectionState.Disconnected -> "Disconnected"
        is ConnectionState.Error -> "Error: $detail"
    },
    isConnected = this is ConnectionState.Connected
)
