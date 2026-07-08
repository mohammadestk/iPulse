package dev.esteki.ipulse.domain.usecase

import dev.esteki.ipulse.domain.repository.FakeBrokerConnection
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class BrokerUseCasesTest {

    private val broker = FakeBrokerConnection()

    @Test
    fun connectToBroker_success() = runTest {
        val result = ConnectToBroker(broker)("broker.mqtt.com", 8081)

        assertTrue(result.isSuccess)
        assertEquals(1, broker.connectCalls.size)
        assertEquals("broker.mqtt.com" to 8081, broker.connectCalls[0])
    }

    @Test
    fun connectToBroker_failure() = runTest {
        broker.connectBehavior = { _, _ -> throw Exception("Connection refused") }

        val result = ConnectToBroker(broker)("broker.mqtt.com", 8081)

        assertTrue(result.isFailure)
    }

    @Test
    fun disconnectFromBroker_success() = runTest {
        val result = DisconnectFromBroker(broker)()

        assertTrue(result.isSuccess)
        assertTrue(broker.disconnectCalled)
    }

    @Test
    fun disconnectFromBroker_failure() = runTest {
        broker.disconnectBehavior = { throw Exception("Disconnect failed") }

        val result = DisconnectFromBroker(broker)()

        assertTrue(result.isFailure)
    }

    @Test
    fun subscribeToDeviceTopic_success() = runTest {
        val result = SubscribeToDeviceTopic(broker)("/esteki/devices")

        assertTrue(result.isSuccess)
        assertEquals(1, broker.subscribeCalls.size)
        assertEquals("/esteki/devices", broker.subscribeCalls[0])
    }

    @Test
    fun subscribeToDeviceTopic_failure() = runTest {
        broker.subscribeBehavior = { throw Exception("Subscribe failed") }

        val result = SubscribeToDeviceTopic(broker)("/esteki/devices")

        assertTrue(result.isFailure)
    }
}
