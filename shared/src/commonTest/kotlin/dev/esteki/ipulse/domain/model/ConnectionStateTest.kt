package dev.esteki.ipulse.domain.model

import com.google.common.truth.Truth.assertThat
import kotlin.test.Test

class ConnectionStateTest {

    @Test
    fun connected_isSingleton() {
        assertThat(ConnectionState.Connected).isSameInstanceAs(ConnectionState.Connected)
    }

    @Test
    fun disconnected_isSingleton() {
        assertThat(ConnectionState.Disconnected).isSameInstanceAs(ConnectionState.Disconnected)
    }

    @Test
    fun reconnecting_isSingleton() {
        assertThat(ConnectionState.Reconnecting).isSameInstanceAs(ConnectionState.Reconnecting)
    }

    @Test
    fun connecting_isSingleton() {
        assertThat(ConnectionState.Connecting).isSameInstanceAs(ConnectionState.Connecting)
    }

    @Test
    fun error_containsDetail() {
        val error = ConnectionState.Error("broker rejected")

        assertThat(error.detail).isEqualTo("broker rejected")
        assertThat(error.cause).isNull()
    }

    @Test
    fun error_containsCause() {
        val cause = RuntimeException("timeout")
        val error = ConnectionState.Error("connection failed", cause)

        assertThat(error.detail).isEqualTo("connection failed")
        assertThat(error.cause).isEqualTo(cause)
    }

    @Test
    fun error_equality() {
        val a = ConnectionState.Error("x")
        val b = ConnectionState.Error("x")
        val c = ConnectionState.Error("y")

        assertThat(a).isEqualTo(b)
        assertThat(a).isNotEqualTo(c)
    }
}
