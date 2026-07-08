package dev.esteki.ipulse.data.local.mapper

import com.google.common.truth.Truth.assertThat
import dev.esteki.ipulse.data.local.entity.DeviceEntity
import dev.esteki.ipulse.domain.model.ConnectionState
import dev.esteki.ipulse.domain.model.Device
import dev.esteki.ipulse.domain.model.SensorType
import dev.esteki.ipulse.domain.model.TelemetryReading
import kotlin.test.Test
import kotlin.time.Instant

class DeviceMapperTest {

    private fun entity(
        id: String = "device-1",
        name: String = "Test Device",
        topic: String = "/esteki/devices",
        sensorType: String = "TEMPERATURE",
        connectionState: String = "Connected",
        latestReadingValue: Double? = null,
        latestReadingTimestamp: Long? = null,
        latestReadingTopic: String? = null
    ) = DeviceEntity(
        id = id,
        name = name,
        topic = topic,
        sensorType = sensorType,
        connectionState = connectionState,
        latestReadingValue = latestReadingValue,
        latestReadingTimestamp = latestReadingTimestamp,
        latestReadingTopic = latestReadingTopic
    )

    private fun device(
        id: String = "device-1",
        name: String = "Test Device",
        topic: String = "/esteki/devices",
        sensorType: SensorType = SensorType.TEMPERATURE,
        connectionState: ConnectionState = ConnectionState.Connected,
        latestReading: TelemetryReading? = null
    ) = Device(
        id = id,
        name = name,
        topic = topic,
        sensorType = sensorType,
        latestReading = latestReading,
        connectionState = connectionState
    )

    // --- DeviceEntity.toDomain() ---

    @Test
    fun entityToDomain_mapsAllFields() {
        val entity = entity(
            id = "d1",
            name = "Ward A",
            topic = "/t",
            sensorType = "HUMIDITY",
            connectionState = "Connected",
            latestReadingValue = 61.0,
            latestReadingTimestamp = 1_720_000_000_000,
            latestReadingTopic = "/t/humidity"
        )

        val domain = entity.toDomain()

        assertThat(domain.id).isEqualTo("d1")
        assertThat(domain.name).isEqualTo("Ward A")
        assertThat(domain.topic).isEqualTo("/t")
        assertThat(domain.sensorType).isEqualTo(SensorType.HUMIDITY)
        assertThat(domain.connectionState).isEqualTo(ConnectionState.Connected)
        assertThat(domain.latestReading).isNotNull()
        assertThat(domain.latestReading!!.value).isEqualTo(61.0)
        assertThat(domain.latestReading!!.topic).isEqualTo("/t/humidity")
    }

    @Test
    fun entityToDomain_noLatestReading() {
        val entity = entity(latestReadingValue = null, latestReadingTimestamp = null)

        val domain = entity.toDomain()

        assertThat(domain.latestReading).isNull()
    }

    @Test
    fun entityToDomain_unknownSensorType_fallsBackToTemperature() {
        val entity = entity(sensorType = "UNKNOWN")

        val domain = entity.toDomain()

        assertThat(domain.sensorType).isEqualTo(SensorType.TEMPERATURE)
    }

    @Test
    fun entityToDomain_connectionStateConnected() {
        assertThat(entity(connectionState = "Connected").toDomain().connectionState)
            .isEqualTo(ConnectionState.Connected)
    }

    @Test
    fun entityToDomain_connectionStateReconnecting() {
        assertThat(entity(connectionState = "Reconnecting").toDomain().connectionState)
            .isEqualTo(ConnectionState.Reconnecting)
    }

    @Test
    fun entityToDomain_connectionStateDisconnected() {
        assertThat(entity(connectionState = "Disconnected").toDomain().connectionState)
            .isEqualTo(ConnectionState.Disconnected)
    }

    @Test
    fun entityToDomain_unknownConnectionState_fallsBackToDisconnected() {
        assertThat(entity(connectionState = "Bogus").toDomain().connectionState)
            .isEqualTo(ConnectionState.Disconnected)
    }

    // --- Device.toEntity() ---

    @Test
    fun deviceToEntity_mapsAllFields() {
        val reading = TelemetryReading(
            id = 0,
            value = 24.6,
            sensorType = SensorType.TEMPERATURE,
            timestamp = Instant.fromEpochMilliseconds(1_720_000_000_000),
            topic = "/t"
        )
        val d = device(
            id = "d1",
            name = "Ward A",
            topic = "/t",
            sensorType = SensorType.PRESSURE,
            connectionState = ConnectionState.Reconnecting,
            latestReading = reading
        )

        val entity = d.toEntity()

        assertThat(entity.id).isEqualTo("d1")
        assertThat(entity.name).isEqualTo("Ward A")
        assertThat(entity.topic).isEqualTo("/t")
        assertThat(entity.sensorType).isEqualTo("PRESSURE")
        assertThat(entity.connectionState).isEqualTo("Reconnecting")
        assertThat(entity.latestReadingValue).isEqualTo(24.6)
        assertThat(entity.latestReadingTimestamp).isEqualTo(1_720_000_000_000)
        assertThat(entity.latestReadingTopic).isEqualTo("/t")
    }

    @Test
    fun deviceToEntity_noLatestReading() {
        val d = device(latestReading = null)

        val entity = d.toEntity()

        assertThat(entity.latestReadingValue).isNull()
        assertThat(entity.latestReadingTimestamp).isNull()
        assertThat(entity.latestReadingTopic).isNull()
    }

    // --- Round-trip ---

    @Test
    fun roundTrip_entityToDomainToEntity() {
        val original = entity(
            id = "d1",
            name = "Ward A",
            topic = "/t",
            sensorType = "HUMIDITY",
            connectionState = "Reconnecting",
            latestReadingValue = 61.0,
            latestReadingTimestamp = 1_720_000_000_000,
            latestReadingTopic = "/t/humidity"
        )

        val domain = original.toDomain()
        val roundTripped = domain.toEntity()

        assertThat(roundTripped.id).isEqualTo(original.id)
        assertThat(roundTripped.name).isEqualTo(original.name)
        assertThat(roundTripped.topic).isEqualTo(original.topic)
        assertThat(roundTripped.sensorType).isEqualTo(original.sensorType)
        assertThat(roundTripped.connectionState).isEqualTo(original.connectionState)
        assertThat(roundTripped.latestReadingValue).isEqualTo(original.latestReadingValue)
        assertThat(roundTripped.latestReadingTimestamp).isEqualTo(original.latestReadingTimestamp)
        assertThat(roundTripped.latestReadingTopic).isEqualTo(original.latestReadingTopic)
    }
}
