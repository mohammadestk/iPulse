package dev.esteki.ipulse.domain.model

enum class ConnectionState {
    CONNECTED,
    CONNECTING,
    RECONNECTING,
    DISCONNECTED,
    ERROR;

    val isActive: Boolean
        get() = this == CONNECTED || this == RECONNECTING

    val displayName: String
        get() = when (this) {
            CONNECTED -> "Connected"
            CONNECTING -> "Connecting"
            RECONNECTING -> "Reconnecting"
            DISCONNECTED -> "Disconnected"
            ERROR -> "Broker rejected"
        }
}
