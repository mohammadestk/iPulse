package dev.esteki.ipulse.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.esteki.ipulse.domain.model.SensorType
import dev.esteki.ipulse.domain.model.TelemetryReading
import dev.esteki.ipulse.domain.repository.DeviceRepository
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class GetDeviceReadingsTest {

    private val repository = mockk<DeviceRepository>()
    private val useCase = GetDeviceReadings(repository)

    private fun reading(value: Double) = TelemetryReading(
        id = 1L,
        value = value,
        sensorType = SensorType.TEMPERATURE,
        timestamp = Instant.fromEpochMilliseconds(1_720_000_000_000),
        topic = "/esteki/devices"
    )

    @Test
    fun returnsFlowFromRepository() = runTest {
        every { repository.observeReadingsForDevice("device-1") } returns flowOf(listOf(reading(24.6)))

        useCase("device-1").test {
            val emissions = awaitItem()
            assertThat(emissions).hasSize(1)
            assertThat(emissions[0].value).isEqualTo(24.6)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun filtersByDeviceId() = runTest {
        every { repository.observeReadingsForDevice("device-1") } returns flowOf(listOf(reading(10.0)))
        every { repository.observeReadingsForDevice("device-2") } returns flowOf(listOf(reading(20.0)))

        useCase("device-1").test {
            val emissions = awaitItem()
            assertThat(emissions).hasSize(1)
            assertThat(emissions[0].value).isEqualTo(10.0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun returnsEmptyForUnknownDevice() = runTest {
        every { repository.observeReadingsForDevice("nonexistent") } returns flowOf(emptyList())

        useCase("nonexistent").test {
            assertThat(awaitItem()).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
