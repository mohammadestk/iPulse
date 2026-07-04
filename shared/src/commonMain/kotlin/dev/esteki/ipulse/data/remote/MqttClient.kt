package dev.esteki.ipulse.data.remote

import dev.esteki.ipulse.data.model.MqttMessage
import kotlinx.coroutines.flow.Flow

interface MqttClient {
    val messages: Flow<MqttMessage>
    val connectionState: Flow<MqttConnectionState>

    suspend fun connect(brokerUrl: String, port: Int)
    suspend fun disconnect()
    suspend fun subscribe(topicFilter: String)
    suspend fun unsubscribe(topicFilter: String)
    suspend fun publish(topic: String, payload: String, qos: Int = 1)
}

enum class MqttConnectionState {
    CONNECTED,
    CONNECTING,
    RECONNECTING,
    DISCONNECTED,
    ERROR
}
