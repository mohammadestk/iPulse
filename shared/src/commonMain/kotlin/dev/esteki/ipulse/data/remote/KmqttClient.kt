package dev.esteki.ipulse.data.remote

import dev.esteki.ipulse.data.model.MqttMessage
import io.github.davidepianca98.MQTTClient
import io.github.davidepianca98.mqtt.MQTTVersion
import io.github.davidepianca98.mqtt.Subscription
import io.github.davidepianca98.mqtt.packets.Qos
import io.github.davidepianca98.mqtt.packets.mqtt.MQTTPublish
import io.github.davidepianca98.mqtt.packets.mqttv5.ReasonCode
import io.github.davidepianca98.mqtt.packets.mqttv5.SubscriptionOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KmqttClient : MqttClient {

    private val _messages = MutableSharedFlow<MqttMessage>(extraBufferCapacity = 64)
    override val messages: SharedFlow<MqttMessage> = _messages.asSharedFlow()

    private val _connectionState = MutableStateFlow(MqttConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<MqttConnectionState> = _connectionState.asStateFlow()

    private var client: MQTTClient? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val subscribedTopics = mutableSetOf<String>()

    override suspend fun connect(brokerUrl: String, port: Int) {
        _connectionState.value = MqttConnectionState.CONNECTING

        try {
            client = MQTTClient(
                mqttVersion = MQTTVersion.MQTT3_1_1,
                address = brokerUrl,
                port = port,
                tls = null,
                publishReceived = { publish ->
                    if (publish is MQTTPublish) {
                        val topic = publish.topicName
                        val payload = publish.payload?.toByteArray()?.decodeToString() ?: ""
                        scope.launch {
                            _messages.emit(MqttMessage(topic = topic, payload = payload, qos = publish.qos.value))
                        }
                    }
                }
            )

            client?.runSuspend()

            _connectionState.value = MqttConnectionState.CONNECTED
            resubscribeAll()
        } catch (e: Exception) {
            e.printStackTrace()
            _connectionState.value = MqttConnectionState.ERROR
        }
    }

    override suspend fun disconnect() {
        try {
            client?.disconnect(ReasonCode.DISCONNECT_WITH_WILL_MESSAGE)
        } catch (_: Exception) {}
        client = null
        subscribedTopics.clear()
        _connectionState.value = MqttConnectionState.DISCONNECTED
    }

    override suspend fun subscribe(topicFilter: String) {
        subscribedTopics.add(topicFilter)
        client?.subscribe(listOf(
            Subscription(topicFilter, SubscriptionOptions(qos = Qos.AT_LEAST_ONCE))
        ))
    }

    override suspend fun unsubscribe(topicFilter: String) {
        subscribedTopics.remove(topicFilter)
        client?.unsubscribe(listOf(topicFilter))
    }

    override suspend fun publish(topic: String, payload: String, qos: Int) {
        val kmqttQos = when (qos) {
            0 -> Qos.AT_MOST_ONCE
            1 -> Qos.AT_LEAST_ONCE
            else -> Qos.EXACTLY_ONCE
        }
        client?.publish(
            retain = false,
            qos = kmqttQos,
            topic = topic,
            payload = payload.encodeToByteArray().toUByteArray()
        )
    }

    private fun resubscribeAll() {
        scope.launch {
            subscribedTopics.forEach { topic ->
                client?.subscribe(listOf(
                    Subscription(topic, SubscriptionOptions(qos = Qos.AT_LEAST_ONCE))
                ))
            }
        }
    }

    fun destroy() {
        client?.disconnect(ReasonCode.DISCONNECT_WITH_WILL_MESSAGE)
        client = null
    }
}
