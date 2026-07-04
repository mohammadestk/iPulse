package dev.esteki.ipulse.ui.model

import dev.esteki.ipulse.domain.model.ConnectionState

data class ConnectionStateUi(
    val state: ConnectionState,
    val displayName: String,
    val isConnected: Boolean
)

fun ConnectionState.toConnectionStateUi(): ConnectionStateUi = ConnectionStateUi(
    state = this,
    displayName = displayName,
    isConnected = this == ConnectionState.CONNECTED
)
