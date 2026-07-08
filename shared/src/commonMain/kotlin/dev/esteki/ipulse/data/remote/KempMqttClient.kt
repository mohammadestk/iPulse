package dev.esteki.ipulse.data.remote

import de.kempmobil.ktor.mqtt.MqttClient
import de.kempmobil.ktor.mqtt.PublishRequest
import de.kempmobil.ktor.mqtt.QoS
import de.kempmobil.ktor.mqtt.Topic
import de.kempmobil.ktor.mqtt.TopicFilter
import de.kempmobil.ktor.mqtt.ws.MqttClient
import dev.esteki.ipulse.data.model.MqttMessage
import dev.esteki.ipulse.domain.model.ConnectionState
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KempMqttClient : dev.esteki.ipulse.data.remote.MqttClient {

    private val _messages = MutableSharedFlow<MqttMessage>(extraBufferCapacity = 64)
    override val messages: SharedFlow<MqttMessage> = _messages.asSharedFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var client: MqttClient? = null
    private var packetsJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val subscribedTopics = mutableSetOf<String>()

    override suspend fun connect(brokerUrl: String, port: Int) {
        _connectionState.value = ConnectionState.Connecting

        try {
            val wsUrl = "wss://$brokerUrl:$port/mqtt"
            client = MqttClient(Url(wsUrl)) { }

            client?.connect()?.onSuccess { connack ->
                if (connack.isSuccess) {
                    _connectionState.value = ConnectionState.Connected
                    resubscribeAll()
                    observePublishedPackets()
                } else {
                    val detail = connack.toString()
                    _connectionState.value = ConnectionState.Error("Broker rejected: $detail")
                }
            }?.onFailure { error ->
                _connectionState.value = ConnectionState.Error(error.message ?: "Connection failed", error)
            }
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error("Connection failed: ${e.message}", e)
        }
    }

    private fun observePublishedPackets() {
        packetsJob?.cancel()
        packetsJob = scope.launch {
            val currentClient = client ?: return@launch
            try {
                currentClient.publishedPackets.collect { publish ->
                    try {
                        val topic = publish.topic.name
                        val payload = publish.payload.toByteArray().decodeToString()
                        val qos = when (publish.qoS) {
                            QoS.AT_MOST_ONCE -> 0
                            QoS.AT_LEAST_ONCE -> 1
                            QoS.EXACTLY_ONE -> 2
                        }
                        _messages.emit(MqttMessage(topic = topic, payload = payload, qos = qos))
                    } catch (_: Exception) {
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    override suspend fun disconnect() {
        packetsJob?.cancel()
        packetsJob = null
        try {
            client?.disconnect()
        } catch (_: Exception) {
        }
        client = null
        subscribedTopics.clear()
        _connectionState.value = ConnectionState.Disconnected
    }

    override suspend fun subscribe(topicFilter: String) {
        try {
            subscribedTopics.add(topicFilter)
            client?.subscribe(listOf(TopicFilter(Topic(topicFilter))))
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error("Subscribe failed: ${e.message}", e)
        }
    }

    override suspend fun unsubscribe(topicFilter: String) {
        try {
            subscribedTopics.remove(topicFilter)
            client?.unsubscribe(listOf(Topic(topicFilter)))
        } catch (_: Exception) {
        }
    }

    override suspend fun publish(topic: String, payload: String, qos: Int) {
        try {
            val mqttQos = when (qos) {
                0 -> QoS.AT_MOST_ONCE
                1 -> QoS.AT_LEAST_ONCE
                else -> QoS.EXACTLY_ONE
            }
            client?.publish(PublishRequest(topic) {
                desiredQoS = mqttQos
                payload(payload)
            })
        } catch (_: Exception) {
        }
    }

    private fun resubscribeAll() {
        scope.launch {
            subscribedTopics.toList().forEach { topic ->
                try {
                    client?.subscribe(listOf(TopicFilter(Topic(topic))))
                } catch (_: Exception) {
                }
            }
        }
    }
}
