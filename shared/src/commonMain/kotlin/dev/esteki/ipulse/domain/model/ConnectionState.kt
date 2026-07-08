package dev.esteki.ipulse.domain.model

sealed interface ConnectionState {
    data object Connected : ConnectionState
    data object Connecting : ConnectionState
    data object Reconnecting : ConnectionState
    data object Disconnected : ConnectionState
    data class Error(val detail: String, val cause: Throwable? = null) : ConnectionState
}
