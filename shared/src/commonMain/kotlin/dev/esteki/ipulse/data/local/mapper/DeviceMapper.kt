package dev.esteki.ipulse.data.local.mapper

import dev.esteki.ipulse.data.local.entity.DeviceEntity
import dev.esteki.ipulse.domain.model.ConnectionState
import dev.esteki.ipulse.domain.model.Device
import dev.esteki.ipulse.domain.model.SensorType
import dev.esteki.ipulse.domain.model.TelemetryReading
import kotlin.time.Instant

fun DeviceEntity.toDomain(): Device {
    val sensorType = SensorType.entries.find { it.name == sensorType } ?: SensorType.TEMPERATURE
    val latestReading = if (latestReadingValue != null && latestReadingTimestamp != null) {
        TelemetryReading(
            value = latestReadingValue,
            sensorType = sensorType,
            timestamp = Instant.fromEpochMilliseconds(latestReadingTimestamp),
            topic = latestReadingTopic ?: topic
        )
    } else null

    return Device(
        id = id,
        name = name,
        topic = topic,
        sensorType = sensorType,
        latestReading = latestReading,
        connectionState = ConnectionState.DISCONNECTED
    )
}

fun Device.toEntity(): DeviceEntity = DeviceEntity(
    id = id,
    name = name,
    topic = topic,
    sensorType = sensorType.name,
    latestReadingValue = latestReading?.value,
    latestReadingTimestamp = latestReading?.timestamp?.toEpochMilliseconds(),
    latestReadingTopic = latestReading?.topic
)
