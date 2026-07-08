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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KempMqttClient(
    private val bufferCapacity: Int = DEFAULT_BUFFER_CAPACITY
) : MqttClientBase {

    private val _messages = MutableSharedFlow<MqttMessage>(extraBufferCapacity = bufferCapacity)
    override val messages: SharedFlow<MqttMessage> = _messages.asSharedFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var transport: MqttClient? = null
    private var packetsJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val subscribedTopics = mutableSetOf<String>()

    override suspend fun connect(brokerUrl: String, port: Int) {
        _connectionState.value = ConnectionState.Connecting
        val wsUrl = Url("wss://$brokerUrl:$port$WS_PATH")

        try {
            transport = MqttClient(wsUrl) { }
            val connack = transport?.connect()
                ?: throw IllegalStateException("MQTT client not created")

            if (!connack.isSuccess) {
                _connectionState.value = ConnectionState.Error("Broker rejected: $connack")
                return
            }

            _connectionState.value = ConnectionState.Connected
            resubscribeAll()
            observePublishedPackets()
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error("Connection failed: ${e.message}", e)
        }
    }

    override suspend fun disconnect() {
        packetsJob?.cancel()
        packetsJob = null
        try {
            transport?.disconnect()
        } catch (_: Exception) {
        }
        transport = null
        subscribedTopics.clear()
        _connectionState.value = ConnectionState.Disconnected
    }

    override suspend fun subscribe(topicFilter: String) {
        try {
            subscribedTopics.add(topicFilter)
            transport?.subscribe(listOf(TopicFilter(Topic(topicFilter))))
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error("Subscribe failed: ${e.message}", e)
        }
    }

    override suspend fun unsubscribe(topicFilter: String) {
        try {
            subscribedTopics.remove(topicFilter)
            transport?.unsubscribe(listOf(Topic(topicFilter)))
        } catch (_: Exception) {
        }
    }

    override suspend fun publish(topic: String, payload: String, qos: Int) {
        try {
            transport?.publish(PublishRequest(topic) {
                desiredQoS = qos.toMqttQoS()
                payload(payload)
            })
        } catch (_: Exception) {
        }
    }

    fun close() {
        packetsJob?.cancel()
        packetsJob = null
        scope.cancel()
        transport = null
    }

    private fun observePublishedPackets() {
        packetsJob?.cancel()
        packetsJob = scope.launch {
            val current = transport ?: return@launch
            try {
                current.publishedPackets.collect { publish ->
                    val topic = publish.topic.name
                    val payload = publish.payload.toByteArray().decodeToString()
                    val qos = publish.qoS.toMqttQoS()
                    _messages.emit(MqttMessage(topic = topic, payload = payload, qos = qos))
                }
            } catch (_: Exception) {
            }
        }
    }

    private suspend fun resubscribeAll() {
        val snapshot = subscribedTopics.toList()
        for (topic in snapshot) {
            try {
                transport?.subscribe(listOf(TopicFilter(Topic(topic))))
            } catch (_: Exception) {
            }
        }
    }

    private fun QoS.toMqttQoS(): Int = when (this) {
        QoS.AT_MOST_ONCE -> 0
        QoS.AT_LEAST_ONCE -> 1
        QoS.EXACTLY_ONE -> 2
    }

    private fun Int.toMqttQoS(): QoS = when (this) {
        0 -> QoS.AT_MOST_ONCE
        1 -> QoS.AT_LEAST_ONCE
        else -> QoS.EXACTLY_ONE
    }

    private companion object {
        const val DEFAULT_BUFFER_CAPACITY = 64
        const val WS_PATH = "/mqtt"
    }
}
