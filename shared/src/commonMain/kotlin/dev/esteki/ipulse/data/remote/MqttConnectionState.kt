package dev.esteki.ipulse.data.remote

sealed interface MqttConnectionState {
    data object Connected : MqttConnectionState
    data object Connecting : MqttConnectionState
    data object Reconnecting : MqttConnectionState
    data object Disconnected : MqttConnectionState
    data class Error(val detail: String, val cause: Throwable? = null) : MqttConnectionState
}
