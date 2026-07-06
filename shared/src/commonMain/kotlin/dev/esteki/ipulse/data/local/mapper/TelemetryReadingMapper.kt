package dev.esteki.ipulse.data.local.mapper

import dev.esteki.ipulse.data.local.entity.TelemetryReadingEntity
import dev.esteki.ipulse.domain.model.SensorType
import dev.esteki.ipulse.domain.model.TelemetryReading
import kotlin.time.Instant

fun TelemetryReadingEntity.toDomain(): TelemetryReading {
    val sensorType = SensorType.entries.find { it.name == sensorType } ?: SensorType.TEMPERATURE
    return TelemetryReading(
        value = value,
        sensorType = sensorType,
        timestamp = Instant.fromEpochMilliseconds(timestamp),
        topic = topic
    )
}

fun TelemetryReading.toEntity(deviceId: String): TelemetryReadingEntity = TelemetryReadingEntity(
    deviceId = deviceId,
    value = value,
    sensorType = sensorType.name,
    timestamp = timestamp.toEpochMilliseconds(),
    topic = topic
)
