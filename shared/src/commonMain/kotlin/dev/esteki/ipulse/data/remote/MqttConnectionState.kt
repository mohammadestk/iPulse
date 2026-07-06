package dev.esteki.ipulse.data.remote

enum class MqttConnectionState {
    CONNECTED,
    CONNECTING,
    RECONNECTING,
    DISCONNECTED,
    ERROR
}
