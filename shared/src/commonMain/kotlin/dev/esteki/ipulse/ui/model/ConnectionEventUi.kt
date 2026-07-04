package dev.esteki.ipulse.ui.model

import dev.esteki.ipulse.domain.model.ConnectionEvent
import dev.esteki.ipulse.domain.model.EventType
import kotlin.time.Instant

data class ConnectionEventUi(
    val timestamp: Instant,
    val type: EventType,
    val message: String,
    val displayTime: String,
    val attempt: Int? = null,
    val backoffSeconds: Double? = null
)

fun ConnectionEvent.toConnectionEventUi(): ConnectionEventUi = ConnectionEventUi(
    timestamp = timestamp,
    type = type,
    message = message,
    displayTime = formatTimestamp(timestamp),
    attempt = attempt,
    backoffSeconds = backoffSeconds
)

private fun formatTimestamp(instant: Instant): String {
    val epochSeconds = instant.epochSeconds
    val hours = (epochSeconds % 86400) / 3600
    val minutes = (epochSeconds % 3600) / 60
    val seconds = epochSeconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}
