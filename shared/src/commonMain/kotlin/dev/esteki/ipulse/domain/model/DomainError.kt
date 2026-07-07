package dev.esteki.ipulse.domain.model

sealed class DomainError(message: String, cause: Throwable? = null) : Throwable(message, cause) {
    class Connection(detail: String, cause: Throwable? = null) : DomainError(detail, cause)
    class DeviceNotFound(val deviceId: String) : DomainError("Device $deviceId not found")
    class Subscription(val topic: String, cause: Throwable? = null) : DomainError("Failed to subscribe to $topic", cause)
    class Unknown(cause: Throwable) : DomainError(cause.message ?: "Unknown error", cause)
}
