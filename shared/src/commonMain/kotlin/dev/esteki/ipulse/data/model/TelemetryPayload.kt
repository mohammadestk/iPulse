package dev.esteki.ipulse.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TelemetryPayload(
    val value: Double,
    val unit: String? = null,
    val timestamp: Long? = null
)
