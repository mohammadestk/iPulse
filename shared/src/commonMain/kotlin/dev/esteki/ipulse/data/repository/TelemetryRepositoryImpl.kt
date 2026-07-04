package dev.esteki.ipulse.data.repository

import dev.esteki.ipulse.data.model.TelemetryPayload
import dev.esteki.ipulse.data.remote.MqttClient
import dev.esteki.ipulse.domain.model.*
import dev.esteki.ipulse.domain.repository.TelemetryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Instant

class TelemetryRepositoryImpl(
    private val mqttClient: MqttClient,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : TelemetryRepository {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _devices = MutableStateFlow<Map<String, Device>>(emptyMap())
    override val devices: Flow<List<Device>> = _devices.map { it.values.toList() }

    private val _signalQuality = MutableStateFlow(
        SignalQuality(averageLatencyMs = 0.0, stability = Stability.NO_DATA)
    )
    override val signalQuality: Flow<SignalQuality> = _signalQuality.asStateFlow()

    private val readings = mutableMapOf<String, MutableList<TelemetryReading>>()

    init {
        observeMqttMessages()
    }

    private fun observeMqttMessages() {
        scope.launch {
            mqttClient.messages.collect { message ->
                processMessage(message.topic, message.payload, Clock.System.now().toEpochMilliseconds())
            }
        }
    }

    override suspend fun processMessage(topic: String, payload: String, timestamp: Long) {
        val sensorType = SensorType.fromTopic(topic) ?: return
        val deviceId = extractDeviceId(topic)
        val instant = Instant.fromEpochMilliseconds(timestamp)

        val reading = TelemetryReading(
            value = parsePayload(payload),
            sensorType = sensorType,
            timestamp = instant,
            topic = topic
        )

        readings.getOrPut(deviceId) { mutableListOf() }.add(reading)

        _devices.update { current ->
            val device = current[deviceId] ?: Device(
                id = deviceId,
                name = formatDeviceName(deviceId),
                topic = topic,
                sensorType = sensorType
            )
            current + (deviceId to device.copy(
                latestReading = reading,
                connectionState = DeviceConnectionState.CONNECTED
            ))
        }

        updateSignalQuality()
    }

    override suspend fun getDeviceById(id: String): Device? {
        return _devices.value[id]
    }

    override suspend fun getReadingsForDevice(deviceId: String): List<TelemetryReading> {
        return readings[deviceId]?.toList() ?: emptyList()
    }

    override suspend fun clearOldReadings(maxAgeMs: Long) {
        val cutoff = Clock.System.now().toEpochMilliseconds() - maxAgeMs
        readings.values.forEach { list ->
            list.removeAll { it.timestamp.toEpochMilliseconds() < cutoff }
        }
    }

    private fun parsePayload(payload: String): Double {
        return try {
            json.decodeFromString<TelemetryPayload>(payload).value
        } catch (_: Exception) {
            payload.trim().toDoubleOrNull() ?: 0.0
        }
    }

    private fun extractDeviceId(topic: String): String {
        val parts = topic.split("/")
        return if (parts.size >= 2) parts[1] else topic
    }

    private fun formatDeviceName(deviceId: String): String {
        return deviceId.replace("-", " ").split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }
    }

    private fun updateSignalQuality() {
        val allReadings = readings.values.flatten()
        if (allReadings.isEmpty()) {
            _signalQuality.value = SignalQuality(0.0, Stability.NO_DATA)
            return
        }

        val recentReadings = allReadings.filter {
            it.timestamp.toEpochMilliseconds() > Clock.System.now().toEpochMilliseconds() - 60000
        }

        if (recentReadings.isEmpty()) {
            _signalQuality.value = SignalQuality(0.0, Stability.NO_DATA)
            return
        }

        val stability = when {
            recentReadings.size > 10 -> Stability.STABLE
            recentReadings.size > 3 -> Stability.JITTERY
            else -> Stability.NO_DATA
        }

        _signalQuality.value = SignalQuality(
            averageLatencyMs = 40.0,
            stability = stability,
            lastReceivedAt = recentReadings.lastOrNull()?.timestamp
        )
    }
}
