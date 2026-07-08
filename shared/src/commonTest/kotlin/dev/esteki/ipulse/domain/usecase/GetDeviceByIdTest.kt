package dev.esteki.ipulse.domain.usecase

import com.google.common.truth.Truth.assertThat
import dev.esteki.ipulse.domain.model.ConnectionState
import dev.esteki.ipulse.domain.model.Device
import dev.esteki.ipulse.domain.model.SensorType
import dev.esteki.ipulse.domain.repository.DeviceRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class GetDeviceByIdTest {

    private val repository = mockk<DeviceRepository>()
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
        coEvery { repository.getDeviceById("d1") } returns Result.success(device("d1", "Ward A"))

        val result = useCase("d1")

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()?.name).isEqualTo("Ward A")
    }

    @Test
    fun returnsFailureForUnknownDevice() = runTest {
        coEvery { repository.getDeviceById("nonexistent") } returns Result.failure(Exception("Not found"))

        val result = useCase("nonexistent")

        assertThat(result.isFailure).isTrue()
    }
}
