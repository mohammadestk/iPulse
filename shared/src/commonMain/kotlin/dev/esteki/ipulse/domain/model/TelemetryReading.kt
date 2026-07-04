package dev.esteki.ipulse.domain.model

import kotlin.time.Instant

data class TelemetryReading(
    val value: Double,
    val sensorType: SensorType,
    val timestamp: Instant,
    val topic: String
)
