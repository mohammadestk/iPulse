package dev.esteki.ipulse.presentation.model

import dev.esteki.ipulse.domain.model.SignalQuality
import dev.esteki.ipulse.domain.model.Stability

data class SignalQualityUi(
    val averageLatencyMs: Double,
    val stability: Stability,
    val displayLabel: String
)

fun SignalQuality.toSignalQualityUi(): SignalQualityUi = SignalQualityUi(
    averageLatencyMs = averageLatencyMs,
    stability = stability,
    displayLabel = when (stability) {
        Stability.STABLE -> "stable · ${averageLatencyMs.toInt()}ms avg"
        Stability.JITTERY -> "jittery · ${averageLatencyMs.toInt()}ms avg"
        Stability.NO_DATA -> "no data"
    }
)
