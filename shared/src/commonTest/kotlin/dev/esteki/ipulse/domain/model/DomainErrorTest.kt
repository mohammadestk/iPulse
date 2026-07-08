package dev.esteki.ipulse.domain.model

import com.google.common.truth.Truth.assertThat
import kotlin.test.Test

class DomainErrorTest {

    @Test
    fun connection_errorMessageContainsUrlAndPort() {
        val error = DomainError.Connection("Failed to connect to broker.mqtt.com:8081")

        assertThat(error.message).isEqualTo("Failed to connect to broker.mqtt.com:8081")
    }

    @Test
    fun connection_causesChained() {
        val cause = RuntimeException("timeout")
        val error = DomainError.Connection("Failed to connect", cause)

        assertThat(error.cause).isEqualTo(cause)
        assertThat(error.cause?.message).isEqualTo("timeout")
    }

    @Test
    fun deviceNotFound_errorMessageContainsDeviceId() {
        val error = DomainError.DeviceNotFound("ward-b-bed-4")

        assertThat(error.message).isEqualTo("Device ward-b-bed-4 not found")
        assertThat(error.deviceId).isEqualTo("ward-b-bed-4")
    }

    @Test
    fun subscription_errorMessageContainsTopic() {
        val error = DomainError.Subscription("/esteki/devices")

        assertThat(error.message).isEqualTo("Failed to subscribe to /esteki/devices")
        assertThat(error.topic).isEqualTo("/esteki/devices")
    }

    @Test
    fun subscription_causesChained() {
        val cause = RuntimeException("network error")
        val error = DomainError.Subscription("/esteki/devices", cause)

        assertThat(error.cause).isEqualTo(cause)
    }

    @Test
    fun unknown_usesCauseMessage() {
        val cause = RuntimeException("something broke")
        val error = DomainError.Unknown(cause)

        assertThat(error.message).isEqualTo("something broke")
        assertThat(error.cause).isEqualTo(cause)
    }

    @Test
    fun unknown_nullCauseMessage_usesFallback() {
        val cause = RuntimeException()
        val error = DomainError.Unknown(cause)

        assertThat(error.message).isNotEmpty()
    }

    @Test
    fun allErrors_areThrowables() {
        val connection: Throwable = DomainError.Connection("x")
        val notFound: Throwable = DomainError.DeviceNotFound("x")
        val subscription: Throwable = DomainError.Subscription("x")
        val unknown: Throwable = DomainError.Unknown(RuntimeException())

        assertThat(connection).isInstanceOf(DomainError::class.java)
        assertThat(notFound).isInstanceOf(DomainError::class.java)
        assertThat(subscription).isInstanceOf(DomainError::class.java)
        assertThat(unknown).isInstanceOf(DomainError::class.java)
    }
}
