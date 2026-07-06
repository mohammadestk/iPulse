package dev.esteki.ipulse.presentation.screen

import kotlin.time.Instant

data class ReadingUi(
    val value: Double,
    val timestamp: Instant,
    val formattedTime: String
)
