package dev.esteki.ipulse.domain.usecase

import dev.esteki.ipulse.domain.model.ConnectionState
import dev.esteki.ipulse.domain.model.Device
import dev.esteki.ipulse.domain.model.SensorType
import dev.esteki.ipulse.domain.repository.FakeDeviceRepository
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class GetDeviceByIdTest {

    private val repository = FakeDeviceRepository()
    private val useCase = GetDeviceById(repository)

    private fun device(id: String = "device-1", name: String = "Test Device") = Device(
        id = id,
        name = name,
        topic = "/esteki/devices",
        sensorType = SensorType.TEMPERATURE,
        latestReading = null,
        connectionState = ConnectionState.Connected
    )

    @Test
    fun returnsDevice() = runTest {
        repository.setDevice(device("d1", "Ward A"))

        val result = useCase("d1")

        assertTrue(result.isSuccess)
        assertEquals("Ward A", result.getOrNull()?.name)
    }

    @Test
    fun returnsFailureForUnknownDevice() = runTest {
        val result = useCase("nonexistent")

        assertTrue(result.isFailure)
    }
}
