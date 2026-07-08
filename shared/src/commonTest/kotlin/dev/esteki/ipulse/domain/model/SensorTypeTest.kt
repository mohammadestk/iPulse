package dev.esteki.ipulse.domain.model

import com.google.common.truth.Truth.assertThat
import kotlin.test.Test

class SensorTypeTest {

    // --- fromTopic() ---

    @Test
    fun fromTopic_temperatureKeyword() {
        assertThat(SensorType.fromTopic("devicepulse/ward-b-bed-4/sensors/temp")).isEqualTo(SensorType.TEMPERATURE)
    }

    @Test
    fun fromTopic_temperatureFullWord() {
        assertThat(SensorType.fromTopic("/esteki/devices/temperature")).isEqualTo(SensorType.TEMPERATURE)
    }

    @Test
    fun fromTopic_pressureKeyword() {
        assertThat(SensorType.fromTopic("devicepulse/ward-b/sensors/pressure")).isEqualTo(SensorType.PRESSURE)
    }

    @Test
    fun fromTopic_humidityKeyword() {
        assertThat(SensorType.fromTopic("devicepulse/ward-b/sensors/humidity")).isEqualTo(SensorType.HUMIDITY)
    }

    @Test
    fun fromTopic_caseInsensitive() {
        assertThat(SensorType.fromTopic("/TEMP/reading")).isEqualTo(SensorType.TEMPERATURE)
        assertThat(SensorType.fromTopic("/Pressure/reading")).isEqualTo(SensorType.PRESSURE)
        assertThat(SensorType.fromTopic("/HUMIDITY/reading")).isEqualTo(SensorType.HUMIDITY)
    }

    @Test
    fun fromTopic_noMatch_returnsNull() {
        assertThat(SensorType.fromTopic("/esteki/devices")).isNull()
        assertThat(SensorType.fromTopic("")).isNull()
        assertThat(SensorType.fromTopic("devicepulse/ward-b-bed-4/sensors/voltage")).isNull()
    }

    @Test
    fun fromTopic_partialMatch_works() {
        assertThat(SensorType.fromTopic("temperature-sensor")).isEqualTo(SensorType.TEMPERATURE)
        assertThat(SensorType.fromTopic("barometric-pressure")).isEqualTo(SensorType.PRESSURE)
        assertThat(SensorType.fromTopic("relative-humidity")).isEqualTo(SensorType.HUMIDITY)
    }

    // --- enum properties ---

    @Test
    fun temperature_hasCorrectProperties() {
        assertThat(SensorType.TEMPERATURE.displayName).isEqualTo("Temperature")
        assertThat(SensorType.TEMPERATURE.unit).isEqualTo("°C")
    }

    @Test
    fun pressure_hasCorrectProperties() {
        assertThat(SensorType.PRESSURE.displayName).isEqualTo("Pressure")
        assertThat(SensorType.PRESSURE.unit).isEqualTo("hPa")
    }

    @Test
    fun humidity_hasCorrectProperties() {
        assertThat(SensorType.HUMIDITY.displayName).isEqualTo("Humidity")
        assertThat(SensorType.HUMIDITY.unit).isEqualTo("%RH")
    }

    @Test
    fun enumHasThreeEntries() {
        assertThat(SensorType.entries).hasSize(3)
    }
}
