package dev.esteki.ipulse.domain.model

import kotlin.time.Instant

data class ConnectionEvent(
    val timestamp: Instant,
    val type: EventType,
    val message: String,
    val attempt: Int? = null,
    val backoffSeconds: Double? = null
)
