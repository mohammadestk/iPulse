package dev.esteki.ipulse.data.local.mapper

import com.google.common.truth.Truth.assertThat
import dev.esteki.ipulse.data.local.entity.TelemetryReadingEntity
import dev.esteki.ipulse.domain.model.SensorType
import dev.esteki.ipulse.domain.model.TelemetryReading
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

        assertThat(domain.id).isEqualTo(42)
        assertThat(domain.value).isEqualTo(-18.2)
        assertThat(domain.sensorType).isEqualTo(SensorType.PRESSURE)
        assertThat(domain.timestamp).isEqualTo(Instant.fromEpochMilliseconds(1_000_000))
        assertThat(domain.topic).isEqualTo("/esteki/devices")
    }

    @Test
    fun entityToDomain_unknownSensorType_fallsBackToTemperature() {
        val e = entity(sensorType = "UNKNOWN")

        assertThat(e.toDomain().sensorType).isEqualTo(SensorType.TEMPERATURE)
    }

    @Test
    fun entityToDomain_humidity() {
        val e = entity(sensorType = "HUMIDITY")

        assertThat(e.toDomain().sensorType).isEqualTo(SensorType.HUMIDITY)
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

        assertThat(entity.deviceId).isEqualTo("device-1")
        assertThat(entity.value).isEqualTo(42.0)
        assertThat(entity.sensorType).isEqualTo("HUMIDITY")
        assertThat(entity.timestamp).isEqualTo(1_720_000_000_000)
        assertThat(entity.topic).isEqualTo("/t")
    }

    @Test
    fun readingToEntity_doesNotCopyId() {
        val reading = TelemetryReading(id = 99, value = 1.0, sensorType = SensorType.TEMPERATURE, timestamp = Instant.fromEpochMilliseconds(0), topic = "/t")

        val entity = reading.toEntity("d1")

        assertThat(entity.id).isEqualTo(0) // auto-generated, not copied from domain
    }

    // --- Round-trip ---

    @Test
    fun roundTrip_entityToDomainToEntity() {
        val original = entity(id = 5, deviceId = "d1", value = 24.6, sensorType = "TEMPERATURE", timestamp = 1_720_000_000_000, topic = "/t")

        val domain = original.toDomain()
        val roundTripped = domain.toEntity("d1")

        assertThat(roundTripped.deviceId).isEqualTo(original.deviceId)
        assertThat(roundTripped.value).isEqualTo(original.value)
        assertThat(roundTripped.sensorType).isEqualTo(original.sensorType)
        assertThat(roundTripped.timestamp).isEqualTo(original.timestamp)
        assertThat(roundTripped.topic).isEqualTo(original.topic)
    }
}
