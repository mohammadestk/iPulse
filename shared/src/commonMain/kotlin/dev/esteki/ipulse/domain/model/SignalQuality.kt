package dev.esteki.ipulse.domain.model

import kotlin.time.Instant

data class SignalQuality(
    val averageLatencyMs: Double,
    val stability: Stability,
    val lastReceivedAt: Instant? = null
)
