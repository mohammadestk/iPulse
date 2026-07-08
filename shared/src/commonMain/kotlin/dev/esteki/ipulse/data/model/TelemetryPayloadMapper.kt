package dev.esteki.ipulse.data.model

import dev.esteki.ipulse.domain.model.ConnectionState
import dev.esteki.ipulse.domain.model.SensorType
import dev.esteki.ipulse.domain.model.TelemetryReading
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Result of decoding a [TelemetryPayload] into the values the domain needs to
 * upsert a device and store a reading. Returned null when the payload cannot
 * be mapped (unknown sensor type) — the caller drops the message in that case,
 * matching the previous behaviour.
 */
data class DecodedTelemetry(
    val deviceId: String,
    val name: String,
    val sensorType: SensorType,
    val connectionState: ConnectionState,
    val reading: TelemetryReading
)

/**
 * Maps a wire [TelemetryPayload] into domain values. Owns the conversion of the
 * payload's string enums ([TelemetryPayload.sensorType], [TelemetryPayload.status])
 * into their domain counterparts, and resolves the optional `unit`/`timestamp`
 * fallbacks. The topic is passed through only so it can be stored on the reading
 * for display — it is never parsed for identity.
 */
fun TelemetryPayload.toDecodedTelemetry(topic: String, now: Instant = Clock.System.now()): DecodedTelemetry? {
    val sensorType = mapSensorType(sensorType) ?: SensorType.fromTopic(topic) ?: return null
    val connectionState = mapStatus(status)
    val timestamp = this.timestamp?.let { Instant.fromEpochMilliseconds(it) } ?: now
    val reading = TelemetryReading(
        value = value,
        sensorType = sensorType,
        timestamp = timestamp,
        topic = topic
    )
    return DecodedTelemetry(
        deviceId = deviceId,
        name = name,
        sensorType = sensorType,
        connectionState = connectionState,
        reading = reading
    )
}

private fun mapSensorType(raw: String): SensorType? =
    SensorType.entries.find { it.name.equals(raw, ignoreCase = true) }

private fun mapStatus(raw: String): ConnectionState = when (raw.lowercase()) {
    "live" -> ConnectionState.Connected
    "reconnecting" -> ConnectionState.Reconnecting
    "offline" -> ConnectionState.Disconnected
    else -> ConnectionState.Disconnected
}
