package dev.esteki.ipulse.domain.model

import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.Test

class ConnectionStateTest {

    @Test
    fun connected_isSingleton() {
        assertSame(ConnectionState.Connected, ConnectionState.Connected)
    }

    @Test
    fun disconnected_isSingleton() {
        assertSame(ConnectionState.Disconnected, ConnectionState.Disconnected)
    }

    @Test
    fun reconnecting_isSingleton() {
        assertSame(ConnectionState.Reconnecting, ConnectionState.Reconnecting)
    }

    @Test
    fun connecting_isSingleton() {
        assertSame(ConnectionState.Connecting, ConnectionState.Connecting)
    }

    @Test
    fun error_containsDetail() {
        val error = ConnectionState.Error("broker rejected")

        assertEquals("broker rejected", error.detail)
        assertNull(error.cause)
    }

    @Test
    fun error_containsCause() {
        val cause = RuntimeException("timeout")
        val error = ConnectionState.Error("connection failed", cause)

        assertEquals("connection failed", error.detail)
        assertEquals(cause, error.cause)
    }

    @Test
    fun error_equality() {
        val a = ConnectionState.Error("x")
        val b = ConnectionState.Error("x")
        val c = ConnectionState.Error("y")

        assertEquals(a, b)
        assertNotEquals(a, c)
    }
}
