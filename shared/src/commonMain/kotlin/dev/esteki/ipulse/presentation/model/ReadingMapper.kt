package dev.esteki.ipulse.presentation.model

import dev.esteki.ipulse.domain.model.TelemetryReading
import dev.esteki.ipulse.presentation.screen.ReadingUi
import kotlin.time.Instant

fun TelemetryReading.toReadingUi(): ReadingUi = ReadingUi(
    id = id,
    value = value,
    timestamp = timestamp,
    formattedTime = formatReadingTime(timestamp)
)

private fun formatReadingTime(instant: Instant): String {
    val epochSeconds = instant.epochSeconds
    val hours = ((epochSeconds % 86400) / 3600).toInt()
    val minutes = ((epochSeconds % 3600) / 60).toInt()
    val seconds = (epochSeconds % 60).toInt()
    return "${hours.pad()}:${minutes.pad()}:${seconds.pad()}"
}

private fun Int.pad(): String = if (this < 10) "0$this" else "$this"
