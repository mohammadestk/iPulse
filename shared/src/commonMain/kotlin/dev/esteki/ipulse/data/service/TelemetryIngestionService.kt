package dev.esteki.ipulse.data.service

import dev.esteki.ipulse.data.local.dao.DeviceDao
import dev.esteki.ipulse.data.local.dao.TelemetryReadingDao
import dev.esteki.ipulse.data.local.mapper.toDomain
import dev.esteki.ipulse.data.local.mapper.toEntity
import dev.esteki.ipulse.data.model.TelemetryPayload
import dev.esteki.ipulse.data.remote.MqttClientBase
import dev.esteki.ipulse.domain.model.Device
import dev.esteki.ipulse.domain.model.SensorType
import dev.esteki.ipulse.domain.model.TelemetryReading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Instant

class TelemetryIngestionService(
    private val mqttClient: MqttClientBase,
    private val deviceDao: DeviceDao,
    private val readingDao: TelemetryReadingDao,
    private val json: Json
) {
    private var scope: CoroutineScope? = null

    fun start() {
        if (scope != null) return
        scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        scope!!.launch { ingest() }
    }

    fun close() {
        scope?.cancel()
        scope = null
    }

    private suspend fun ingest() {
        mqttClient.messages
            .conflate()
            .catch { /* MQTT message flow ended */ }
            .collect { message -> processMessage(message.topic, message.payload) }
    }

    private suspend fun processMessage(topic: String, payload: String) {
        val sensorType = SensorType.fromTopic(topic) ?: return
        val reading = parseReading(topic, payload, Clock.System.now()) ?: return
        val deviceId = extractDeviceId(topic)

        val device = deviceDao.getById(deviceId)?.toDomain()?.copy(latestReading = reading) ?: Device(
            id = deviceId,
            name = formatDeviceName(deviceId),
            topic = topic,
            sensorType = sensorType,
            latestReading = reading
        )

        deviceDao.upsert(device.toEntity())
        readingDao.insert(reading.toEntity(deviceId))
    }

    private fun parseReading(topic: String, payload: String, now: Instant): TelemetryReading? {
        val sensorType = SensorType.fromTopic(topic) ?: return null
        val value = parseValue(payload) ?: return null
        return TelemetryReading(
            value = value,
            sensorType = sensorType,
            timestamp = now,
            topic = topic
        )
    }

    private fun parseValue(payload: String): Double? {
        return try {
            json.decodeFromString<TelemetryPayload>(payload).value
        } catch (_: Exception) {
            payload.trim().toDoubleOrNull()
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
}
