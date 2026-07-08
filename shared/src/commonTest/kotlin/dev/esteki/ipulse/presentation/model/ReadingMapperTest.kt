package dev.esteki.ipulse.presentation.model

import com.google.common.truth.Truth.assertThat
import dev.esteki.ipulse.domain.model.SensorType
import dev.esteki.ipulse.domain.model.TelemetryReading
import kotlin.test.Test
import kotlin.time.Instant

class ReadingMapperTest {

    private fun reading(
        id: Long = 1L,
        value: Double = 24.6,
        sensorType: SensorType = SensorType.TEMPERATURE,
        timestamp: Long = 1_720_000_000_000,
        topic: String = "/esteki/devices"
    ) = TelemetryReading(
        id = id,
        value = value,
        sensorType = sensorType,
        timestamp = Instant.fromEpochMilliseconds(timestamp),
        topic = topic
    )

    @Test
    fun mapsValueAndTimestamp() {
        val ui = reading(value = 42.0, timestamp = 1_700_000_000_000).toReadingUi()

        assertThat(ui.value).isEqualTo(42.0)
        assertThat(ui.timestamp).isEqualTo(Instant.fromEpochMilliseconds(1_700_000_000_000))
    }

    @Test
    fun mapsId() {
        val ui = reading(id = 99L).toReadingUi()

        assertThat(ui.id).isEqualTo(99L)
    }

    @Test
    fun formattedTimeUsesHHmmss() {
        val ui = reading(timestamp = 0).toReadingUi()

        assertThat(ui.formattedTime).isEqualTo("00:00:00")
    }

    @Test
    fun formattedTimePadsSingleDigits() {
        val ui = reading(timestamp = 3_661_000).toReadingUi()

        assertThat(ui.formattedTime).isEqualTo("01:01:01")
    }

    @Test
    fun negativeValueMapsCorrectly() {
        val ui = reading(value = -18.2).toReadingUi()

        assertThat(ui.value).isEqualTo(-18.2)
    }
}
