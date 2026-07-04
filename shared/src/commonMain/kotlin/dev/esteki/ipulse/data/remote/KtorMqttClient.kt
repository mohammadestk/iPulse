package dev.esteki.ipulse.data.remote

import dev.esteki.ipulse.data.model.MqttMessage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.utils.io.core.toByteArray
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

class KtorMqttClient(
    private val httpClient: HttpClient
) : MqttClient {

    private val _messages = MutableSharedFlow<MqttMessage>(extraBufferCapacity = 64)
    override val messages: SharedFlow<MqttMessage> = _messages.asSharedFlow()

    private val _connectionState = MutableStateFlow(MqttConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<MqttConnectionState> = _connectionState.asStateFlow()

    private var webSocketSession: WebSocketSession? = null
    private var receiveJob: Job? = null
    private var reconnectJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var brokerUrl: String = ""
    private var brokerPort: Int = 8080
    private var packetId: Int = 0
    private val subscribedTopics = mutableSetOf<String>()

    override suspend fun connect(brokerUrl: String, port: Int) {
        this.brokerUrl = brokerUrl
        this.brokerPort = port
        _connectionState.value = MqttConnectionState.CONNECTING

        try {
            val wsUrl = "wss://$brokerUrl:$port/mqtt"
            httpClient.webSocket(wsUrl) {
                webSocketSession = this
                sendMqttConnect()
                val connAck = receiveMqttPacket()
                if (connAck != null && connAck.first() == 0x20.toByte()) {
                    _connectionState.value = MqttConnectionState.CONNECTED
                    startReceiving()
                    resubscribeAll()
                } else {
                    _connectionState.value = MqttConnectionState.ERROR
                }
            }
        } catch (_: Exception) {
            _connectionState.value = MqttConnectionState.ERROR
            scheduleReconnect()
        }
    }

    override suspend fun disconnect() {
        reconnectJob?.cancel()
        receiveJob?.cancel()
        subscribedTopics.clear()
        try {
            sendMqttDisconnect()
        } catch (_: Exception) {
        }
        webSocketSession?.close()
        webSocketSession = null
        _connectionState.value = MqttConnectionState.DISCONNECTED
    }

    override suspend fun subscribe(topicFilter: String) {
        subscribedTopics.add(topicFilter)
        sendMqttSubscribe(topicFilter)
    }

    override suspend fun unsubscribe(topicFilter: String) {
        subscribedTopics.remove(topicFilter)
        sendMqttUnsubscribe(topicFilter)
    }

    override suspend fun publish(topic: String, payload: String, qos: Int) {
        sendMqttPublish(topic, payload.toByteArray(), qos)
    }

    private suspend fun sendMqttConnect() {
        val clientId = "ipulse-${kotlin.random.Random.nextInt(10000)}"
        val clientBytes = clientId.toByteArray()

        val variableHeader = byteArrayOf(
            0x00, 0x04, 0x4D, 0x51, 0x54, 0x54, // "MQTT"
            0x04, // Protocol Level (3.1.1)
            0x02, // Connect Flags (Clean Session)
            0x00, 0x3C // Keep Alive (60s)
        )

        val payload = byteArrayOf(0x00, clientBytes.size.toByte()) + clientBytes
        val remainingLength = variableHeader.size + payload.size
        val packet =
            byteArrayOf(0x10.toByte()) + encodeRemainingLength(remainingLength) + variableHeader + payload

        webSocketSession?.send(Frame.Binary(true, packet))
    }

    private suspend fun sendMqttDisconnect() {
        webSocketSession?.send(Frame.Binary(true, byteArrayOf(0xE0.toByte(), 0x00)))
    }

    private suspend fun sendMqttSubscribe(topicFilter: String) {
        val id = nextPacketId()
        val topicBytes = topicFilter.toByteArray()
        val remainingLength = 2 + 2 + topicBytes.size + 1
        val packet = byteArrayOf(0x82.toByte()) +
                encodeRemainingLength(remainingLength) +
                byteArrayOf((id shr 8).toByte(), id.toByte(), 0x00, topicBytes.size.toByte()) +
                topicBytes + 0x01.toByte()

        webSocketSession?.send(Frame.Binary(true, packet))
    }

    private suspend fun sendMqttUnsubscribe(topicFilter: String) {
        val id = nextPacketId()
        val topicBytes = topicFilter.toByteArray()
        val remainingLength = 2 + 2 + topicBytes.size
        val packet = byteArrayOf(0xA2.toByte()) +
                encodeRemainingLength(remainingLength) +
                byteArrayOf((id shr 8).toByte(), id.toByte(), 0x00, topicBytes.size.toByte()) +
                topicBytes

        webSocketSession?.send(Frame.Binary(true, packet))
    }

    private suspend fun sendMqttPublish(topic: String, payload: ByteArray, qos: Int) {
        val topicBytes = topic.toByteArray()
        val header = (0x30 or ((qos and 0x03) shl 1)).toByte()
        val remainingLength = 2 + topicBytes.size + payload.size
        val packet = byteArrayOf(header) +
                encodeRemainingLength(remainingLength) +
                byteArrayOf((topicBytes.size shr 8).toByte(), topicBytes.size.toByte()) +
                topicBytes + payload

        webSocketSession?.send(Frame.Binary(true, packet))
    }

    private fun sendMqttPingReq() {
        scope.launch {
            try {
                webSocketSession?.send(Frame.Binary(true, byteArrayOf(0xC0.toByte(), 0x00)))
            } catch (_: Exception) {
            }
        }
    }

    private fun startReceiving() {
        val session = webSocketSession ?: return
        receiveJob = scope.launch {
            try {
                while (isActive) {
                    val frame = session.incoming.receive() as? Frame.Binary ?: break
                    handleMqttPacket(frame.readBytes())
                }
            } catch (_: Exception) {
                if (_connectionState.value == MqttConnectionState.CONNECTED) {
                    _connectionState.value = MqttConnectionState.RECONNECTING
                    scheduleReconnect()
                }
            }
        }
    }

    private fun handleMqttPacket(data: ByteArray) {
        if (data.isEmpty()) return
        val packetType = (data[0].toInt() and 0xF0) shr 4
        when (packetType) {
            0x03 -> handlePublish(data)
            0x0D -> sendMqttPingReq()
        }
    }

    private fun handlePublish(data: ByteArray) {
        var offset = 1
        val topicLength =
            (data[offset].toInt() and 0xFF) shl 8 or (data[offset + 1].toInt() and 0xFF)
        offset += 2
        val topic = data.decodeToString(offset, offset + topicLength)
        offset += topicLength

        val qos = (data[0].toInt() and 0x06) shr 1
        if (qos > 0) offset += 2

        val payload = data.decodeToString(offset, data.size)
        scope.launch { _messages.emit(MqttMessage(topic = topic, payload = payload, qos = qos)) }
    }

    private fun resubscribeAll() {
        scope.launch {
            subscribedTopics.forEach { sendMqttSubscribe(it) }
        }
    }

    private fun scheduleReconnect() {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(1.seconds)
            repeat(5) { attempt ->
                _connectionState.value = MqttConnectionState.RECONNECTING
                try {
                    connect(brokerUrl, brokerPort)
                    return@launch
                } catch (_: Exception) {
                    delay((1L shl attempt).seconds)
                }
            }
            _connectionState.value = MqttConnectionState.DISCONNECTED
        }
    }

    private fun nextPacketId(): Int {
        packetId = (packetId + 1) % 65536
        if (packetId == 0) packetId = 1
        return packetId
    }

    private fun encodeRemainingLength(length: Int): ByteArray {
        val bytes = mutableListOf<Byte>()
        var x = length
        do {
            var byte = (x % 128).toByte()
            x /= 128
            if (x > 0) byte = (byte.toInt() or 0x80).toByte()
            bytes.add(byte)
        } while (x > 0)
        return bytes.toByteArray()
    }

    private suspend fun receiveMqttPacket(): ByteArray? {
        val session = webSocketSession ?: return null
        return withTimeoutOrNull(5.seconds) {
            (session.incoming.receive() as? Frame.Binary)?.readBytes()
        }
    }

    fun destroy() {
        scope.cancel()
    }
}
