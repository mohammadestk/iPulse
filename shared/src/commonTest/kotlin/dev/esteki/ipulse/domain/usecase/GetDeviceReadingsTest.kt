package dev.esteki.ipulse.domain.usecase

import app.cash.turbine.test
import dev.esteki.ipulse.domain.model.SensorType
import dev.esteki.ipulse.domain.model.TelemetryReading
import dev.esteki.ipulse.domain.repository.FakeDeviceRepository
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.Test
import kotlin.time.Instant
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class GetDeviceReadingsTest {

    private val repository = FakeDeviceRepository()
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
        repository.setReadings("device-1", listOf(reading(24.6)))

        useCase("device-1").test {
            val emissions = awaitItem()
            assertEquals(1, emissions.size)
            assertEquals(24.6, emissions[0].value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun filtersByDeviceId() = runTest {
        repository.setReadings("device-1", listOf(reading(10.0)))
        repository.setReadings("device-2", listOf(reading(20.0)))

        useCase("device-1").test {
            val emissions = awaitItem()
            assertEquals(1, emissions.size)
            assertEquals(10.0, emissions[0].value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun returnsEmptyForUnknownDevice() = runTest {
        useCase("nonexistent").test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
