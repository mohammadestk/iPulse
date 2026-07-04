package dev.esteki.ipulse.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MqttMessage(
    val topic: String,
    val payload: String,
    val qos: Int = 1,
    val retain: Boolean = false
)

@Serializable
data class TelemetryPayload(
    val value: Double,
    val unit: String? = null,
    val timestamp: Long? = null
)
