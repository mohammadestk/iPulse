package dev.esteki.ipulse.domain.model

data class Device(
    val id: String,
    val name: String,
    val topic: String,
    val sensorType: SensorType,
    val latestReading: TelemetryReading? = null,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED
)
