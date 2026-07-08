package dev.esteki.ipulse.data.service

import dev.esteki.ipulse.data.local.dao.DeviceDao
import dev.esteki.ipulse.data.local.dao.TelemetryReadingDao
import dev.esteki.ipulse.data.local.entity.DeviceEntity
import dev.esteki.ipulse.data.local.entity.TelemetryReadingEntity
import dev.esteki.ipulse.data.model.TelemetryPayload
import dev.esteki.ipulse.data.remote.MqttClientAdapter
import dev.esteki.ipulse.domain.model.SensorType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.Clock

class TelemetryIngestionService(
    private val mqttClient: MqttClientAdapter,
    private val deviceDao: DeviceDao,
    private val readingDao: TelemetryReadingDao,
    private val json: Json
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val messageChannel = Channel<MessageEvent>(Channel.BUFFERED)

    init {
        scope.launch { observeMqttMessages() }
        scope.launch { processMessageChannel() }
    }

    private suspend fun observeMqttMessages() {
        mqttClient.messages.collect { message ->
            messageChannel.send(
                MessageEvent(
                    topic = message.topic,
                    payload = message.payload,
                    timestamp = Clock.System.now().toEpochMilliseconds()
                )
            )
        }
    }

    private suspend fun processMessageChannel() {
        for (event in messageChannel) {
            try {
                processMessage(event)
            } catch (_: Exception) {
            }
        }
    }

    private suspend fun processMessage(event: MessageEvent) {
        val sensorType = SensorType.fromTopic(event.topic) ?: return
        val deviceId = extractDeviceId(event.topic)
        val readingValue = parsePayload(event.payload)

        val existingDevice = deviceDao.getById(deviceId)
        val deviceEntity = if (existingDevice != null) {
            existingDevice.copy(
                latestReadingValue = readingValue,
                latestReadingTimestamp = event.timestamp,
                latestReadingTopic = event.topic
            )
        } else {
            DeviceEntity(
                id = deviceId,
                name = formatDeviceName(deviceId),
                topic = event.topic,
                sensorType = sensorType.name,
                latestReadingValue = readingValue,
                latestReadingTimestamp = event.timestamp,
                latestReadingTopic = event.topic
            )
        }

        deviceDao.upsert(deviceEntity)
        readingDao.insert(
            TelemetryReadingEntity(
                deviceId = deviceId,
                value = readingValue,
                sensorType = sensorType.name,
                timestamp = event.timestamp,
                topic = event.topic
            )
        )
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

    private data class MessageEvent(
        val topic: String,
        val payload: String,
        val timestamp: Long
    )
}
