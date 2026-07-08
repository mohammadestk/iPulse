package dev.esteki.ipulse.data.model

import kotlinx.serialization.Serializable

/**
 * Wire contract for a single sensor reading published to the MQTT broker.
 *
 * One message = one reading from one sensor stream, plus the stream's current
 * connection status. The payload is the single source of truth for device
 * identity, display name, sensor type, value, and status — the MQTT topic is
 * used only for subscription routing, never for parsing identity.
 *
 * Wire shape is defined by `specs/telemetry-payload.schema.json`.
 * `unit` and `timestamp` are optional: the consumer falls back to the
 * sensor-type unit and the receive time respectively. The Json instance is
 * configured with `ignoreUnknownKeys`, so extra fields are tolerated.
 */
@Serializable
data class TelemetryPayload(
    val deviceId: String,
    val name: String,
    val sensorType: String,
    val value: Double,
    val unit: String? = null,
    val timestamp: Long? = null,
    val status: String
)
