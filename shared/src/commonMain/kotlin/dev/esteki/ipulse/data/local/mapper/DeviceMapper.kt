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
        connectionState = mapConnectionState(connectionState)
    )
}

fun Device.toEntity(): DeviceEntity = DeviceEntity(
    id = id,
    name = name,
    topic = topic,
    sensorType = sensorType.name,
    connectionState = connectionState.toStateName(),
    latestReadingValue = latestReading?.value,
    latestReadingTimestamp = latestReading?.timestamp?.toEpochMilliseconds(),
    latestReadingTopic = latestReading?.topic
)

private fun mapConnectionState(raw: String): ConnectionState = when (raw) {
    ConnectionState.Connected::class.simpleName -> ConnectionState.Connected
    ConnectionState.Reconnecting::class.simpleName -> ConnectionState.Reconnecting
    ConnectionState.Disconnected::class.simpleName -> ConnectionState.Disconnected
    else -> ConnectionState.Disconnected
}

private fun ConnectionState.toStateName(): String = when (this) {
    is ConnectionState.Connected -> ConnectionState.Connected::class.simpleName!!
    is ConnectionState.Reconnecting -> ConnectionState.Reconnecting::class.simpleName!!
    is ConnectionState.Disconnected -> ConnectionState.Disconnected::class.simpleName!!
    else -> ConnectionState.Disconnected::class.simpleName!!
}
