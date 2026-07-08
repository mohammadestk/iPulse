package dev.esteki.ipulse.domain.model

import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.Test

class SensorTypeTest {

    // --- fromTopic() ---

    @Test
    fun fromTopic_temperatureKeyword() {
        assertEquals(SensorType.TEMPERATURE, SensorType.fromTopic("devicepulse/ward-b-bed-4/sensors/temp"))
    }

    @Test
    fun fromTopic_temperatureFullWord() {
        assertEquals(SensorType.TEMPERATURE, SensorType.fromTopic("/esteki/devices/temperature"))
    }

    @Test
    fun fromTopic_pressureKeyword() {
        assertEquals(SensorType.PRESSURE, SensorType.fromTopic("devicepulse/ward-b/sensors/pressure"))
    }

    @Test
    fun fromTopic_humidityKeyword() {
        assertEquals(SensorType.HUMIDITY, SensorType.fromTopic("devicepulse/ward-b/sensors/humidity"))
    }

    @Test
    fun fromTopic_caseInsensitive() {
        assertEquals(SensorType.TEMPERATURE, SensorType.fromTopic("/TEMP/reading"))
        assertEquals(SensorType.PRESSURE, SensorType.fromTopic("/Pressure/reading"))
        assertEquals(SensorType.HUMIDITY, SensorType.fromTopic("/HUMIDITY/reading"))
    }

    @Test
    fun fromTopic_noMatch_returnsNull() {
        assertNull(SensorType.fromTopic("/esteki/devices"))
        assertNull(SensorType.fromTopic(""))
        assertNull(SensorType.fromTopic("devicepulse/ward-b-bed-4/sensors/voltage"))
    }

    @Test
    fun fromTopic_partialMatch_works() {
        assertEquals(SensorType.TEMPERATURE, SensorType.fromTopic("temperature-sensor"))
        assertEquals(SensorType.PRESSURE, SensorType.fromTopic("barometric-pressure"))
        assertEquals(SensorType.HUMIDITY, SensorType.fromTopic("relative-humidity"))
    }

    // --- enum properties ---

    @Test
    fun temperature_hasCorrectProperties() {
        assertEquals("Temperature", SensorType.TEMPERATURE.displayName)
        assertEquals("°C", SensorType.TEMPERATURE.unit)
    }

    @Test
    fun pressure_hasCorrectProperties() {
        assertEquals("Pressure", SensorType.PRESSURE.displayName)
        assertEquals("hPa", SensorType.PRESSURE.unit)
    }

    @Test
    fun humidity_hasCorrectProperties() {
        assertEquals("Humidity", SensorType.HUMIDITY.displayName)
        assertEquals("%RH", SensorType.HUMIDITY.unit)
    }

    @Test
    fun enumHasThreeEntries() {
        assertEquals(3, SensorType.entries.size)
    }
}
