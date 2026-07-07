package dev.esteki.ipulse.data.repository

import dev.esteki.ipulse.data.local.dao.DeviceDao
import dev.esteki.ipulse.data.local.dao.TelemetryReadingDao
import dev.esteki.ipulse.data.local.entity.DeviceEntity
import dev.esteki.ipulse.data.local.entity.TelemetryReadingEntity
import dev.esteki.ipulse.data.local.mapper.toDomain
import dev.esteki.ipulse.data.model.TelemetryPayload
import dev.esteki.ipulse.data.remote.MqttClientAdapter
import dev.esteki.ipulse.domain.model.*
import dev.esteki.ipulse.domain.repository.DeviceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.Clock

class DeviceRepositoryImpl(
    private val mqttClient: MqttClientAdapter,
    private val deviceDao: DeviceDao,
    private val readingDao: TelemetryReadingDao,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : DeviceRepository {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _signalQuality = MutableStateFlow(
        SignalQuality(averageLatencyMs = 0.0, stability = Stability.NO_DATA)
    )
    override val signalQuality: Flow<SignalQuality> = _signalQuality.asStateFlow()

    override val devices: Flow<List<Device>> = deviceDao.observeAll().map { entities ->
        entities.map { it.toDomain() }
    }

    init {
        observeMqttMessages()
    }

    private fun observeMqttMessages() {
        scope.launch {
            try {
                mqttClient.messages.collect { message ->
                    try {
                        processMessage(
                            topic = message.topic,
                            payload = message.payload,
                            timestamp = Clock.System.now().toEpochMilliseconds()
                        )
                    } catch (_: Exception) {
                        // Skip malformed messages; continue processing
                    }
                }
            } catch (_: Exception) {
                // Message flow ended; telemetry collection stopped
            }
        }
    }

    private fun processMessage(topic: String, payload: String, timestamp: Long) {
        val sensorType = SensorType.fromTopic(topic) ?: return
        val deviceId = extractDeviceId(topic)
        val readingValue = parsePayload(payload)

        scope.launch {
            try {
                val existingDevice = deviceDao.getById(deviceId)

                val deviceEntity = if (existingDevice != null) {
                    existingDevice.copy(
                        latestReadingValue = readingValue,
                        latestReadingTimestamp = timestamp,
                        latestReadingTopic = topic
                    )
                } else {
                    DeviceEntity(
                        id = deviceId,
                        name = formatDeviceName(deviceId),
                        topic = topic,
                        sensorType = sensorType.name,
                        latestReadingValue = readingValue,
                        latestReadingTimestamp = timestamp,
                        latestReadingTopic = topic
                    )
                }

                deviceDao.upsert(deviceEntity)

                readingDao.insert(
                    TelemetryReadingEntity(
                        deviceId = deviceId,
                        value = readingValue,
                        sensorType = sensorType.name,
                        timestamp = timestamp,
                        topic = topic
                    )
                )

                updateSignalQuality()
            } catch (_: Exception) {
                // DB operation failed for this message; skip
            }
        }
    }

    override suspend fun getDeviceById(id: String): Result<Device> {
        return try {
            val entity = deviceDao.getById(id)
            if (entity != null) {
                Result.success(entity.toDomain())
            } else {
                Result.failure(DomainError.DeviceNotFound(id))
            }
        } catch (e: Exception) {
            Result.failure(DomainError.Unknown(e))
        }
    }

    override suspend fun getReadingsForDevice(deviceId: String): Result<List<TelemetryReading>> {
        return try {
            val readings = readingDao.getByDeviceId(deviceId).map { it.toDomain() }
            Result.success(readings)
        } catch (e: Exception) {
            Result.failure(DomainError.Unknown(e))
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

    private suspend fun updateSignalQuality() {
        try {
            val entities = deviceDao.getAll()
            if (entities.isEmpty()) {
                _signalQuality.value = SignalQuality(0.0, Stability.NO_DATA)
                return
            }

            val now = Clock.System.now().toEpochMilliseconds()
            val recentCount = entities.count { device ->
                val timestamp = device.latestReadingTimestamp ?: 0
                timestamp > now - 60000
            }

            val stability = when {
                recentCount > 10 -> Stability.STABLE
                recentCount > 3 -> Stability.JITTERY
                else -> Stability.NO_DATA
            }

            val lastTimestamp = entities
                .mapNotNull { it.latestReadingTimestamp }
                .maxByOrNull { it } ?: 0L

            _signalQuality.value = SignalQuality(
                averageLatencyMs = 40.0,
                stability = stability,
                lastReceivedAt = if (lastTimestamp > 0) kotlin.time.Instant.fromEpochMilliseconds(lastTimestamp) else null
            )
        } catch (_: Exception) {
            // Signal quality update failed; keep previous value
        }
    }
}
