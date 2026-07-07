package dev.esteki.ipulse.domain.model

sealed interface ConnectionState {
    data object Connected : ConnectionState
    data object Connecting : ConnectionState
    data object Reconnecting : ConnectionState
    data object Disconnected : ConnectionState
    data class Error(val detail: String, val cause: Throwable? = null) : ConnectionState

    val isActive: Boolean
        get() = this is Connected || this is Reconnecting

    val displayName: String
        get() = when (this) {
            is Connected -> "Connected"
            is Connecting -> "Connecting"
            is Reconnecting -> "Reconnecting"
            is Disconnected -> "Disconnected"
            is Error -> "Error: $detail"
        }
}
