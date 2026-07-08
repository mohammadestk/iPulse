package dev.esteki.ipulse.data.service

import dev.esteki.ipulse.data.local.dao.DeviceDao
import dev.esteki.ipulse.data.local.dao.TelemetryReadingDao
import dev.esteki.ipulse.data.local.mapper.toDomain
import dev.esteki.ipulse.data.local.mapper.toEntity
import dev.esteki.ipulse.data.model.TelemetryPayload
import dev.esteki.ipulse.data.model.toDecodedTelemetry
import dev.esteki.ipulse.data.remote.MqttClientBase
import dev.esteki.ipulse.domain.model.Device
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.Clock

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
        println("topic: $topic, payload: $payload")
        val decoded = decodePayload(topic, payload) ?: return
        val existing = deviceDao.getById(decoded.deviceId)?.toDomain()
        val device = existing?.copy(
            name = decoded.name,
            sensorType = decoded.sensorType,
            latestReading = decoded.reading,
            connectionState = decoded.connectionState
        ) ?: Device(
            id = decoded.deviceId,
            name = decoded.name,
            topic = topic,
            sensorType = decoded.sensorType,
            latestReading = decoded.reading,
            connectionState = decoded.connectionState
        )

        deviceDao.upsert(device.toEntity())
        readingDao.insert(decoded.reading.toEntity(decoded.deviceId))
    }

    private fun decodePayload(topic: String, payload: String) =
        try {
            json.decodeFromString<TelemetryPayload>(payload).toDecodedTelemetry(topic)
        } catch (_: Exception) {
            null
        }
}
