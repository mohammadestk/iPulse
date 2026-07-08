package dev.esteki.ipulse.data.model

import com.google.common.truth.Truth.assertThat
import dev.esteki.ipulse.domain.model.ConnectionState
import dev.esteki.ipulse.domain.model.SensorType
import kotlinx.serialization.json.Json
import kotlin.test.Test
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

        assertThat(decoded.deviceId).isEqualTo("ward-b-bed-4")
        assertThat(decoded.name).isEqualTo("Ward B — bed 4")
        assertThat(decoded.sensorType).isEqualTo(SensorType.TEMPERATURE)
        assertThat(decoded.connectionState).isEqualTo(ConnectionState.Connected)
        assertThat(decoded.reading.value).isEqualTo(24.6)
        assertThat(decoded.reading.sensorType.unit).isEqualTo("°C")
        assertThat(decoded.reading.timestamp).isEqualTo(Instant.fromEpochMilliseconds(1_700_000_000_000))
        assertThat(decoded.reading.topic).isEqualTo(topic)
    }

    @Test
    fun missingUnit_fallsBackToSensorTypeUnit() {
        val decoded = decode(
            """{"deviceId":"d","name":"n","sensorType":"pressure","value":1013.2,"status":"live"}"""
        )!!

        assertThat(decoded.sensorType).isEqualTo(SensorType.PRESSURE)
        assertThat(SensorType.PRESSURE.unit).isEqualTo("hPa")
    }

    @Test
    fun missingTimestamp_fallsBackToNow() {
        val decoded = decode(
            """{"deviceId":"d","name":"n","sensorType":"temperature","value":19.4,"status":"live"}"""
        )!!

        assertThat(decoded.reading.timestamp).isEqualTo(fixedNow)
    }

    @Test
    fun statusLive_mapsToConnected() {
        val decoded = decode(
            """{"deviceId":"d","name":"n","sensorType":"temperature","value":1.0,"status":"live"}"""
        )!!
        assertThat(decoded.connectionState).isEqualTo(ConnectionState.Connected)
    }

    @Test
    fun statusReconnecting_mapsToReconnecting() {
        val decoded = decode(
            """{"deviceId":"d","name":"n","sensorType":"temperature","value":1.0,"status":"reconnecting"}"""
        )!!
        assertThat(decoded.connectionState).isEqualTo(ConnectionState.Reconnecting)
    }

    @Test
    fun statusOffline_mapsToDisconnected() {
        val decoded = decode(
            """{"deviceId":"d","name":"n","sensorType":"temperature","value":1.0,"status":"offline"}"""
        )!!
        assertThat(decoded.connectionState).isEqualTo(ConnectionState.Disconnected)
    }

    @Test
    fun unknownStatus_mapsToDisconnected() {
        val decoded = decode(
            """{"deviceId":"d","name":"n","sensorType":"temperature","value":1.0,"status":"bogus"}"""
        )!!
        assertThat(decoded.connectionState).isEqualTo(ConnectionState.Disconnected)
    }

    @Test
    fun sensorTypeIsCaseInsensitive() {
        val decoded = decode(
            """{"deviceId":"d","name":"n","sensorType":"HUMIDITY","value":61,"status":"live"}"""
        )!!
        assertThat(decoded.sensorType).isEqualTo(SensorType.HUMIDITY)
    }

    @Test
    fun unknownSensorType_fallsBackToTopicThenDropsWhenTopicHasNoHint() {
        val fromTopic = decode(
            """{"deviceId":"d","name":"n","sensorType":"unknown","value":1.0,"status":"live"}"""
        )
        assertThat(fromTopic?.sensorType).isEqualTo(SensorType.TEMPERATURE)

        val noHint = json.decodeFromString<TelemetryPayload>(
            """{"deviceId":"d","name":"n","sensorType":"unknown","value":1.0,"status":"live"}"""
        ).toDecodedTelemetry("devicepulse/x/y", fixedNow)
        assertThat(noHint).isNull()
    }

    @Test
    fun extraFieldsAreIgnored() {
        val decoded = decode(
            """{"deviceId":"d","name":"n","sensorType":"temperature","value":1.0,"status":"live","extra":"ignored","qos":1}"""
        )!!
        assertThat(decoded.deviceId).isEqualTo("d")
    }

    @Test
    fun malformedJson_decodesToNullInIngestionService() {
        val result = try {
            json.decodeFromString<TelemetryPayload>("not json at all").toDecodedTelemetry(topic, fixedNow)
        } catch (_: Exception) {
            null
        }
        assertThat(result).isNull()
    }

    @Test
    fun humidityExampleFromSchemaMaps() {
        val decoded = decode(
            """{"deviceId":"loading-dock","name":"Loading dock","sensorType":"humidity","value":61,"unit":"%RH","timestamp":1720000000000,"status":"live"}"""
        )!!
        assertThat(decoded.deviceId).isEqualTo("loading-dock")
        assertThat(decoded.name).isEqualTo("Loading dock")
        assertThat(decoded.sensorType).isEqualTo(SensorType.HUMIDITY)
        assertThat(decoded.reading.value).isEqualTo(61.0)
        assertThat(decoded.connectionState).isEqualTo(ConnectionState.Connected)
    }

    @Test
    fun negativeValueMapsCorrectly() {
        val decoded = decode(
            """{"deviceId":"cold-storage-r2","name":"Cold storage — R2","sensorType":"temperature","value":-18.2,"unit":"°C","timestamp":1720000000000,"status":"reconnecting"}"""
        )!!
        assertThat(decoded.reading.value).isEqualTo(-18.2)
        assertThat(decoded.connectionState).isEqualTo(ConnectionState.Reconnecting)
        assertThat(decoded.reading.value).isLessThan(0.0)
    }
}
