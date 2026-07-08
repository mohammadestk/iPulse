package dev.esteki.ipulse.data.local.mapper

import dev.esteki.ipulse.data.local.entity.TelemetryReadingEntity
import dev.esteki.ipulse.domain.model.SensorType
import dev.esteki.ipulse.domain.model.TelemetryReading
import kotlin.test.assertEquals
import kotlin.test.Test
import kotlin.time.Instant

class TelemetryReadingMapperTest {

    private fun entity(
        id: Long = 1L,
        deviceId: String = "device-1",
        value: Double = 24.6,
        sensorType: String = "TEMPERATURE",
        timestamp: Long = 1_720_000_000_000,
        topic: String = "/esteki/devices"
    ) = TelemetryReadingEntity(
        id = id,
        deviceId = deviceId,
        value = value,
        sensorType = sensorType,
        timestamp = timestamp,
        topic = topic
    )

    // --- TelemetryReadingEntity.toDomain() ---

    @Test
    fun entityToDomain_mapsAllFields() {
        val e = entity(id = 42, value = -18.2, sensorType = "PRESSURE", timestamp = 1_000_000)

        val domain = e.toDomain()

        assertEquals(42, domain.id)
        assertEquals(-18.2, domain.value)
        assertEquals(SensorType.PRESSURE, domain.sensorType)
        assertEquals(Instant.fromEpochMilliseconds(1_000_000), domain.timestamp)
        assertEquals("/esteki/devices", domain.topic)
    }

    @Test
    fun entityToDomain_unknownSensorType_fallsBackToTemperature() {
        val e = entity(sensorType = "UNKNOWN")

        assertEquals(SensorType.TEMPERATURE, e.toDomain().sensorType)
    }

    @Test
    fun entityToDomain_humidity() {
        val e = entity(sensorType = "HUMIDITY")

        assertEquals(SensorType.HUMIDITY, e.toDomain().sensorType)
    }

    // --- TelemetryReading.toEntity() ---

    @Test
    fun readingToEntity_mapsAllFields() {
        val reading = TelemetryReading(
            id = 99,
            value = 42.0,
            sensorType = SensorType.HUMIDITY,
            timestamp = Instant.fromEpochMilliseconds(1_720_000_000_000),
            topic = "/t"
        )

        val entity = reading.toEntity("device-1")

        assertEquals("device-1", entity.deviceId)
        assertEquals(42.0, entity.value)
        assertEquals("HUMIDITY", entity.sensorType)
        assertEquals(1_720_000_000_000, entity.timestamp)
        assertEquals("/t", entity.topic)
    }

    @Test
    fun readingToEntity_doesNotCopyId() {
        val reading = TelemetryReading(id = 99, value = 1.0, sensorType = SensorType.TEMPERATURE, timestamp = Instant.fromEpochMilliseconds(0), topic = "/t")

        val entity = reading.toEntity("d1")

        assertEquals(0, entity.id) // auto-generated, not copied from domain
    }

    // --- Round-trip ---

    @Test
    fun roundTrip_entityToDomainToEntity() {
        val original = entity(id = 5, deviceId = "d1", value = 24.6, sensorType = "TEMPERATURE", timestamp = 1_720_000_000_000, topic = "/t")

        val domain = original.toDomain()
        val roundTripped = domain.toEntity("d1")

        assertEquals(original.deviceId, roundTripped.deviceId)
        assertEquals(original.value, roundTripped.value)
        assertEquals(original.sensorType, roundTripped.sensorType)
        assertEquals(original.timestamp, roundTripped.timestamp)
        assertEquals(original.topic, roundTripped.topic)
    }
}
