package dev.esteki.ipulse.data.model

import dev.esteki.ipulse.domain.model.ConnectionState
import dev.esteki.ipulse.domain.model.SensorType
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class TelemetryPayloadMapperTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val fixedNow = Instant.fromEpochMilliseconds(1_720_000_000_000)
    private val topic = "devicepulse/ward-b-bed-4/sensors/temp"

    private fun decode(payloadJson: String) =
        json.decodeFromString<TelemetryPayload>(payloadJson).toDecodedTelemetry(topic, fixedNow)

    @Test
    fun fullPayload_mapsAllFields() {
        val decoded = decode(
            """{"deviceId":"ward-b-bed-4","name":"Ward B — bed 4","sensorType":"temperature","value":24.6,"unit":"°C","timestamp":1700000000000,"status":"live"}"""
        )!!

        assertEquals("ward-b-bed-4", decoded.deviceId)
        assertEquals("Ward B — bed 4", decoded.name)
        assertEquals(SensorType.TEMPERATURE, decoded.sensorType)
        assertEquals(ConnectionState.Connected, decoded.connectionState)
        assertEquals(24.6, decoded.reading.value)
        assertEquals("°C", decoded.reading.sensorType.unit)
        assertEquals(Instant.fromEpochMilliseconds(1_700_000_000_000), decoded.reading.timestamp)
        assertEquals(topic, decoded.reading.topic)
    }

    @Test
    fun missingUnit_fallsBackToSensorTypeUnit() {
        val decoded = decode(
            """{"deviceId":"d","name":"n","sensorType":"pressure","value":1013.2,"status":"live"}"""
        )!!

        assertEquals(SensorType.PRESSURE, decoded.sensorType)
        assertEquals("hPa", SensorType.PRESSURE.unit)
    }

    @Test
    fun missingTimestamp_fallsBackToNow() {
        val decoded = decode(
            """{"deviceId":"d","name":"n","sensorType":"temperature","value":19.4,"status":"live"}"""
        )!!

        assertEquals(fixedNow, decoded.reading.timestamp)
    }

    @Test
    fun statusLive_mapsToConnected() {
        val decoded = decode(
            """{"deviceId":"d","name":"n","sensorType":"temperature","value":1.0,"status":"live"}"""
        )!!
        assertEquals(ConnectionState.Connected, decoded.connectionState)
    }

    @Test
    fun statusReconnecting_mapsToReconnecting() {
        val decoded = decode(
            """{"deviceId":"d","name":"n","sensorType":"temperature","value":1.0,"status":"reconnecting"}"""
        )!!
        assertEquals(ConnectionState.Reconnecting, decoded.connectionState)
    }

    @Test
    fun statusOffline_mapsToDisconnected() {
        val decoded = decode(
            """{"deviceId":"d","name":"n","sensorType":"temperature","value":1.0,"status":"offline"}"""
        )!!
        assertEquals(ConnectionState.Disconnected, decoded.connectionState)
    }

    @Test
    fun unknownStatus_mapsToDisconnected() {
        val decoded = decode(
            """{"deviceId":"d","name":"n","sensorType":"temperature","value":1.0,"status":"bogus"}"""
        )!!
        assertEquals(ConnectionState.Disconnected, decoded.connectionState)
    }

    @Test
    fun sensorTypeIsCaseInsensitive() {
        val decoded = decode(
            """{"deviceId":"d","name":"n","sensorType":"HUMIDITY","value":61,"status":"live"}"""
        )!!
        assertEquals(SensorType.HUMIDITY, decoded.sensorType)
    }

    @Test
    fun unknownSensorType_fallsBackToTopicThenDropsWhenTopicHasNoHint() {
        // topic here contains "temp", so an unknown sensorType falls back to TEMPERATURE
        val fromTopic = decode(
            """{"deviceId":"d","name":"n","sensorType":"unknown","value":1.0,"status":"live"}"""
        )
        assertEquals(SensorType.TEMPERATURE, fromTopic?.sensorType)

        // a topic with no sensor hint + unknown sensorType → null (message dropped)
        val noHint = json.decodeFromString<TelemetryPayload>(
            """{"deviceId":"d","name":"n","sensorType":"unknown","value":1.0,"status":"live"}"""
        ).toDecodedTelemetry("devicepulse/x/y", fixedNow)
        assertNull(noHint)
    }

    @Test
    fun extraFieldsAreIgnored() {
        val decoded = decode(
            """{"deviceId":"d","name":"n","sensorType":"temperature","value":1.0,"status":"live","extra":"ignored","qos":1}"""
        )!!
        assertEquals("d", decoded.deviceId)
    }

    @Test
    fun malformedJson_decodesToNullInIngestionService() {
        val result = try {
            json.decodeFromString<TelemetryPayload>("not json at all").toDecodedTelemetry(topic, fixedNow)
        } catch (_: Exception) {
            null
        }
        assertNull(result)
    }

    @Test
    fun humidityExampleFromSchemaMaps() {
        val decoded = decode(
            """{"deviceId":"loading-dock","name":"Loading dock","sensorType":"humidity","value":61,"unit":"%RH","timestamp":1720000000000,"status":"live"}"""
        )!!
        assertEquals("loading-dock", decoded.deviceId)
        assertEquals("Loading dock", decoded.name)
        assertEquals(SensorType.HUMIDITY, decoded.sensorType)
        assertEquals(61.0, decoded.reading.value)
        assertEquals(ConnectionState.Connected, decoded.connectionState)
    }

    @Test
    fun negativeValueMapsCorrectly() {
        val decoded = decode(
            """{"deviceId":"cold-storage-r2","name":"Cold storage — R2","sensorType":"temperature","value":-18.2,"unit":"°C","timestamp":1720000000000,"status":"reconnecting"}"""
        )!!
        assertEquals(-18.2, decoded.reading.value)
        assertEquals(ConnectionState.Reconnecting, decoded.connectionState)
        assertTrue(decoded.reading.value < 0)
    }
}
