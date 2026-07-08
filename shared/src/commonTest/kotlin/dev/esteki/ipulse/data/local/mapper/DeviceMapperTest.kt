package dev.esteki.ipulse.data.local.mapper

import dev.esteki.ipulse.data.local.entity.DeviceEntity
import dev.esteki.ipulse.domain.model.ConnectionState
import dev.esteki.ipulse.domain.model.Device
import dev.esteki.ipulse.domain.model.SensorType
import dev.esteki.ipulse.domain.model.TelemetryReading
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
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

        assertEquals("d1", domain.id)
        assertEquals("Ward A", domain.name)
        assertEquals("/t", domain.topic)
        assertEquals(SensorType.HUMIDITY, domain.sensorType)
        assertEquals(ConnectionState.Connected, domain.connectionState)
        assertNotNull(domain.latestReading)
        assertEquals(61.0, domain.latestReading.value)
        assertEquals("/t/humidity", domain.latestReading.topic)
    }

    @Test
    fun entityToDomain_noLatestReading() {
        val entity = entity(latestReadingValue = null, latestReadingTimestamp = null)

        val domain = entity.toDomain()

        assertNull(domain.latestReading)
    }

    @Test
    fun entityToDomain_unknownSensorType_fallsBackToTemperature() {
        val entity = entity(sensorType = "UNKNOWN")

        val domain = entity.toDomain()

        assertEquals(SensorType.TEMPERATURE, domain.sensorType)
    }

    @Test
    fun entityToDomain_connectionStateConnected() {
        assertEquals(ConnectionState.Connected, entity(connectionState = "Connected").toDomain().connectionState)
    }

    @Test
    fun entityToDomain_connectionStateReconnecting() {
        assertEquals(ConnectionState.Reconnecting, entity(connectionState = "Reconnecting").toDomain().connectionState)
    }

    @Test
    fun entityToDomain_connectionStateDisconnected() {
        assertEquals(ConnectionState.Disconnected, entity(connectionState = "Disconnected").toDomain().connectionState)
    }

    @Test
    fun entityToDomain_unknownConnectionState_fallsBackToDisconnected() {
        assertEquals(ConnectionState.Disconnected, entity(connectionState = "Bogus").toDomain().connectionState)
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

        assertEquals("d1", entity.id)
        assertEquals("Ward A", entity.name)
        assertEquals("/t", entity.topic)
        assertEquals("PRESSURE", entity.sensorType)
        assertEquals("Reconnecting", entity.connectionState)
        assertEquals(24.6, entity.latestReadingValue)
        assertEquals(1_720_000_000_000, entity.latestReadingTimestamp)
        assertEquals("/t", entity.latestReadingTopic)
    }

    @Test
    fun deviceToEntity_noLatestReading() {
        val d = device(latestReading = null)

        val entity = d.toEntity()

        assertNull(entity.latestReadingValue)
        assertNull(entity.latestReadingTimestamp)
        assertNull(entity.latestReadingTopic)
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

        assertEquals(original.id, roundTripped.id)
        assertEquals(original.name, roundTripped.name)
        assertEquals(original.topic, roundTripped.topic)
        assertEquals(original.sensorType, roundTripped.sensorType)
        assertEquals(original.connectionState, roundTripped.connectionState)
        assertEquals(original.latestReadingValue, roundTripped.latestReadingValue)
        assertEquals(original.latestReadingTimestamp, roundTripped.latestReadingTimestamp)
        assertEquals(original.latestReadingTopic, roundTripped.latestReadingTopic)
    }
}
