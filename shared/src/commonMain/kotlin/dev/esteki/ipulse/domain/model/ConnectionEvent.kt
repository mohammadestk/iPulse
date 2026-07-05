package dev.esteki.ipulse.domain.model

import kotlin.time.Instant

enum class EventType {
    CONNECTED, CONNECTION_LOST, RECONNECTING, DISCONNECTED, ERROR
}

data class ConnectionEvent(
    val timestamp: Instant,
    val type: EventType,
    val message: String,
    val attempt: Int? = null,
    val backoffSeconds: Double? = null
)
