package dev.esteki.ipulse.domain.model

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.Test

class DomainErrorTest {

    @Test
    fun connection_errorMessageContainsUrlAndPort() {
        val error = DomainError.Connection("Failed to connect to broker.mqtt.com:8081")

        assertEquals("Failed to connect to broker.mqtt.com:8081", error.message)
    }

    @Test
    fun connection_causesChained() {
        val cause = RuntimeException("timeout")
        val error = DomainError.Connection("Failed to connect", cause)

        assertEquals(cause, error.cause)
        assertEquals("timeout", error.cause?.message)
    }

    @Test
    fun deviceNotFound_errorMessageContainsDeviceId() {
        val error = DomainError.DeviceNotFound("ward-b-bed-4")

        assertEquals("Device ward-b-bed-4 not found", error.message)
        assertEquals("ward-b-bed-4", error.deviceId)
    }

    @Test
    fun subscription_errorMessageContainsTopic() {
        val error = DomainError.Subscription("/esteki/devices")

        assertEquals("Failed to subscribe to /esteki/devices", error.message)
        assertEquals("/esteki/devices", error.topic)
    }

    @Test
    fun subscription_causesChained() {
        val cause = RuntimeException("network error")
        val error = DomainError.Subscription("/esteki/devices", cause)

        assertEquals(cause, error.cause)
    }

    @Test
    fun unknown_usesCauseMessage() {
        val cause = RuntimeException("something broke")
        val error = DomainError.Unknown(cause)

        assertEquals("something broke", error.message)
        assertEquals(cause, error.cause)
    }

    @Test
    fun unknown_nullCauseMessage_usesFallback() {
        val cause = RuntimeException()
        val error = DomainError.Unknown(cause)

        assertTrue(!error.message.isNullOrEmpty())
    }

    @Test
    fun allErrors_areThrowables() {
        val connection: Throwable = DomainError.Connection("x")
        val notFound: Throwable = DomainError.DeviceNotFound("x")
        val subscription: Throwable = DomainError.Subscription("x")
        val unknown: Throwable = DomainError.Unknown(RuntimeException())

        assertTrue(connection is DomainError)
        assertTrue(notFound is DomainError)
        assertTrue(subscription is DomainError)
        assertTrue(unknown is DomainError)
    }
}
