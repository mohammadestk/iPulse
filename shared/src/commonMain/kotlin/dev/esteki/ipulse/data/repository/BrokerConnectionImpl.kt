package dev.esteki.ipulse.data.repository

import dev.esteki.ipulse.data.remote.MqttClientAdapter
import dev.esteki.ipulse.data.remote.MqttConnectionState
import dev.esteki.ipulse.domain.model.ConnectionEvent
import dev.esteki.ipulse.domain.model.ConnectionState
import dev.esteki.ipulse.domain.model.EventType
import dev.esteki.ipulse.domain.repository.BrokerConnection
import dev.esteki.ipulse.domain.repository.BrokerMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.math.pow

class BrokerConnectionImpl(
    private val mqttClient: MqttClientAdapter
) : BrokerConnection {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var brokerUrl: String = ""
    private var brokerPort: Int = 0
    private var reconnectAttempt = 0
    private var reconnectJob: Job? = null
    private var shouldReconnect = true

    private val _connectionEvents = MutableSharedFlow<ConnectionEvent>(extraBufferCapacity = 64)
    override val connectionEvents: Flow<ConnectionEvent> = _connectionEvents.asSharedFlow()

    override val connectionState: Flow<ConnectionState> = mqttClient.connectionState.map { state ->
        when (state) {
            MqttConnectionState.Connected -> ConnectionState.Connected
            MqttConnectionState.Connecting -> ConnectionState.Connecting
            MqttConnectionState.Reconnecting -> ConnectionState.Reconnecting
            MqttConnectionState.Disconnected -> ConnectionState.Disconnected
            is MqttConnectionState.Error -> ConnectionState.Error(state.detail, state.cause)
        }
    }

    override val messages: Flow<BrokerMessage> = mqttClient.messages.map { msg ->
        BrokerMessage(topic = msg.topic, payload = msg.payload)
    }

    init {
        scope.launch {
            mqttClient.connectionState.collect { state ->
                handleConnectionStateChange(state)
            }
        }
    }

    private suspend fun handleConnectionStateChange(state: MqttConnectionState) {
        val now = Clock.System.now()
        when (state) {
            MqttConnectionState.Connected -> {
                reconnectAttempt = 0
                reconnectJob?.cancel()
                reconnectJob = null
                _connectionEvents.emit(
                    ConnectionEvent(timestamp = now, type = EventType.CONNECTED, message = "Connected")
                )
            }
            MqttConnectionState.Connecting -> {
                // No event for transient connecting state
            }
            MqttConnectionState.Reconnecting -> {
                _connectionEvents.emit(
                    ConnectionEvent(timestamp = now, type = EventType.RECONNECTING, message = "Reconnecting")
                )
            }
            MqttConnectionState.Disconnected -> {
                reconnectJob?.cancel()
                reconnectJob = null
                _connectionEvents.emit(
                    ConnectionEvent(timestamp = now, type = EventType.DISCONNECTED, message = "Disconnected")
                )
            }
            is MqttConnectionState.Error -> {
                _connectionEvents.emit(
                    ConnectionEvent(
                        timestamp = now,
                        type = EventType.ERROR,
                        message = state.detail
                    )
                )
                if (shouldReconnect && reconnectJob?.isActive != true) {
                    startReconnection()
                }
            }
        }
    }

    private fun startReconnection() {
        reconnectJob = scope.launch {
            while (true) {
                val currentState = (mqttClient.connectionState as? kotlinx.coroutines.flow.StateFlow)?.value
                if (currentState !is MqttConnectionState.Error || !shouldReconnect) break

                reconnectAttempt++
                val backoff = calculateBackoff(reconnectAttempt)

                _connectionEvents.emit(
                    ConnectionEvent(
                        timestamp = Clock.System.now(),
                        type = EventType.RECONNECTING,
                        message = "Reconnecting (attempt $reconnectAttempt)",
                        attempt = reconnectAttempt,
                        backoffSeconds = backoff.inWholeMilliseconds.toDouble() / 1000.0
                    )
                )

                delay(backoff)

                if (!shouldReconnect) break

                mqttClient.connect(brokerUrl, brokerPort)
            }
        }
    }

    private fun calculateBackoff(attempt: Int): Duration {
        val baseDelay = 1.seconds
        val maxDelay = 60.seconds
        val rawDelay = baseDelay.inWholeMilliseconds * 2.0.pow((attempt - 1).toDouble())
        val clampedDelay = rawDelay.toLong().coerceAtMost(maxDelay.inWholeMilliseconds)
        val jitter = (clampedDelay * 0.25).toLong()
        val jitterOffset = (-jitter..jitter).random()
        return (clampedDelay + jitterOffset).milliseconds
    }

    override suspend fun connect(brokerUrl: String, port: Int) {
        this.brokerUrl = brokerUrl
        this.brokerPort = port
        shouldReconnect = true
        reconnectAttempt = 0
        mqttClient.connect(brokerUrl, port)
    }

    override suspend fun disconnect() {
        shouldReconnect = false
        reconnectJob?.cancel()
        reconnectJob = null
        mqttClient.disconnect()
    }

    override suspend fun subscribe(topicFilter: String) {
        mqttClient.subscribe(topicFilter)
    }
}
