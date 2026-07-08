package dev.esteki.ipulse.domain.usecase

import com.google.common.truth.Truth.assertThat
import dev.esteki.ipulse.domain.repository.BrokerConnection
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class BrokerUseCasesTest {

    private val broker = mockk<BrokerConnection>(relaxed = true)

    @Test
    fun connectToBroker_success() = runTest {
        coEvery { broker.connect("broker.mqtt.com", 8081) } returns Unit

        val result = ConnectToBroker(broker)("broker.mqtt.com", 8081)

        assertThat(result.isSuccess).isTrue()
        coVerify { broker.connect("broker.mqtt.com", 8081) }
    }

    @Test
    fun connectToBroker_failure() = runTest {
        coEvery { broker.connect(any(), any()) } throws Exception("Connection refused")

        val result = ConnectToBroker(broker)("broker.mqtt.com", 8081)

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun disconnectFromBroker_success() = runTest {
        coEvery { broker.disconnect() } returns Unit

        val result = DisconnectFromBroker(broker)()

        assertThat(result.isSuccess).isTrue()
        coVerify { broker.disconnect() }
    }

    @Test
    fun disconnectFromBroker_failure() = runTest {
        coEvery { broker.disconnect() } throws Exception("Disconnect failed")

        val result = DisconnectFromBroker(broker)()

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun subscribeToDeviceTopic_success() = runTest {
        coEvery { broker.subscribe("/esteki/devices") } returns Unit

        val result = SubscribeToDeviceTopic(broker)("/esteki/devices")

        assertThat(result.isSuccess).isTrue()
        coVerify { broker.subscribe("/esteki/devices") }
    }

    @Test
    fun subscribeToDeviceTopic_failure() = runTest {
        coEvery { broker.subscribe(any()) } throws Exception("Subscribe failed")

        val result = SubscribeToDeviceTopic(broker)("/esteki/devices")

        assertThat(result.isFailure).isTrue()
    }
}
