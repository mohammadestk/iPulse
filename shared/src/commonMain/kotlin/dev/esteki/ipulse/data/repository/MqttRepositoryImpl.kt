package dev.esteki.ipulse.data.repository

import dev.esteki.ipulse.data.remote.MqttClientAdapter
import dev.esteki.ipulse.data.remote.MqttConnectionState
import dev.esteki.ipulse.domain.model.ConnectionEvent
import dev.esteki.ipulse.domain.model.ConnectionState
import dev.esteki.ipulse.domain.model.EventType
import dev.esteki.ipulse.domain.repository.MqttRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.Instant

class MqttRepositoryImpl(
    private val mqttClient: MqttClientAdapter
) : MqttRepository {

    override val connectionState: Flow<ConnectionState> = mqttClient.connectionState.map { state ->
        when (state) {
            MqttConnectionState.CONNECTED -> ConnectionState.CONNECTED
            MqttConnectionState.CONNECTING -> ConnectionState.CONNECTING
            MqttConnectionState.RECONNECTING -> ConnectionState.RECONNECTING
            MqttConnectionState.DISCONNECTED -> ConnectionState.DISCONNECTED
            MqttConnectionState.ERROR -> ConnectionState.ERROR
        }
    }

    override val connectionEvents: Flow<ConnectionEvent> = mqttClient.connectionState.map { state ->
        val now = Clock.System.now()
        val eventType = when (state) {
            MqttConnectionState.CONNECTED -> EventType.CONNECTED
            MqttConnectionState.CONNECTING -> EventType.RECONNECTING
            MqttConnectionState.RECONNECTING -> EventType.RECONNECTING
            MqttConnectionState.DISCONNECTED -> EventType.DISCONNECTED
            MqttConnectionState.ERROR -> EventType.ERROR
        }
        val message = when (state) {
            MqttConnectionState.CONNECTED -> "Connected"
            MqttConnectionState.CONNECTING -> "Connecting"
            MqttConnectionState.RECONNECTING -> "Reconnecting"
            MqttConnectionState.DISCONNECTED -> "Disconnected"
            MqttConnectionState.ERROR -> "Connection error"
        }
        ConnectionEvent(timestamp = now, type = eventType, message = message)
    }

    override suspend fun connect(brokerUrl: String, port: Int) {
        mqttClient.connect(brokerUrl, port)
    }

    override suspend fun disconnect() {
        mqttClient.disconnect()
    }

    override suspend fun subscribe(topicFilter: String) {
        mqttClient.subscribe(topicFilter)
    }

    override suspend fun unsubscribe(topicFilter: String) {
        mqttClient.unsubscribe(topicFilter)
    }

    override suspend fun publish(topic: String, payload: String, qos: Int) {
        mqttClient.publish(topic, payload, qos)
    }
}
