package dev.esteki.ipulse.data.remote

import dev.esteki.ipulse.data.model.MqttMessage
import de.kempmobil.ktor.mqtt.QoS
import de.kempmobil.ktor.mqtt.Topic
import de.kempmobil.ktor.mqtt.TopicFilter
import io.ktor.http.Url
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

class KtorMqttClient : MqttClientAdapter {

    private val _messages = MutableSharedFlow<MqttMessage>(extraBufferCapacity = 64)
    override val messages: SharedFlow<MqttMessage> = _messages.asSharedFlow()

    private val _connectionState = MutableStateFlow(MqttConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<MqttConnectionState> = _connectionState.asStateFlow()

    private var client: de.kempmobil.ktor.mqtt.MqttClient? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val subscribedTopics = mutableSetOf<String>()

    override suspend fun connect(brokerUrl: String, port: Int) {
        _connectionState.value = MqttConnectionState.CONNECTING

        try {
            val wsUrl = "wss://$brokerUrl:$port/mqtt"
            client = de.kempmobil.ktor.mqtt.ws.MqttClient(Url(wsUrl)) {
                // No additional config needed
            }

            client?.connect()?.onSuccess { connack ->
                if (connack.isSuccess) {
                    _connectionState.value = MqttConnectionState.CONNECTED
                    resubscribeAll()
                    observePublishedPackets()
                } else {
                    _connectionState.value = MqttConnectionState.ERROR
                }
            }?.onFailure {
                it.printStackTrace()
                _connectionState.value = MqttConnectionState.ERROR
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _connectionState.value = MqttConnectionState.ERROR
        }
    }

    private fun observePublishedPackets() {
        scope.launch {
            client?.publishedPackets?.collect { publish ->
                val topic = publish.topic.name
                val payload = publish.payload.toByteArray().decodeToString()
                val qos = when (publish.qoS) {
                    QoS.AT_MOST_ONCE -> 0
                    QoS.AT_LEAST_ONCE -> 1
                    QoS.EXACTLY_ONE -> 2
                }
                _messages.emit(MqttMessage(topic = topic, payload = payload, qos = qos))
            }
        }
    }

    override suspend fun disconnect() {
        try {
            client?.disconnect()
        } catch (_: Exception) {}
        client = null
        subscribedTopics.clear()
        _connectionState.value = MqttConnectionState.DISCONNECTED
    }

    override suspend fun subscribe(topicFilter: String) {
        subscribedTopics.add(topicFilter)
        client?.subscribe(listOf(TopicFilter(Topic(topicFilter))))
    }

    override suspend fun unsubscribe(topicFilter: String) {
        subscribedTopics.remove(topicFilter)
        client?.unsubscribe(listOf(Topic(topicFilter)))
    }

    override suspend fun publish(topic: String, payload: String, qos: Int) {
        val mqttQos = when (qos) {
            0 -> QoS.AT_MOST_ONCE
            1 -> QoS.AT_LEAST_ONCE
            else -> QoS.EXACTLY_ONE
        }
        client?.publish(de.kempmobil.ktor.mqtt.PublishRequest(topic) {
            desiredQoS = mqttQos
            payload(payload)
        })
    }

    private fun resubscribeAll() {
        scope.launch {
            subscribedTopics.forEach { topic ->
                client?.subscribe(listOf(TopicFilter(Topic(topic))))
            }
        }
    }

    fun destroy() {
        scope.launch {
            client?.disconnect()
        }
        client = null
    }
}
