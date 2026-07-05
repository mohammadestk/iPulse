package dev.esteki.ipulse.presentation.model

import dev.esteki.ipulse.domain.model.Device
import dev.esteki.ipulse.domain.model.ConnectionState
import dev.esteki.ipulse.domain.model.SensorType

data class DeviceUi(
    val id: String,
    val name: String,
    val topic: String,
    val sensorType: SensorType,
    val latestValue: String,
    val unit: String,
    val connectionState: ConnectionState,
    val isLive: Boolean
)

fun Device.toDeviceUi(): DeviceUi = DeviceUi(
    id = id,
    name = name,
    topic = topic,
    sensorType = sensorType,
    latestValue = latestReading?.value?.format(1) ?: "--",
    unit = sensorType.unit,
    connectionState = connectionState,
    isLive = connectionState == ConnectionState.CONNECTED
)

private fun Double.format(decimals: Int): String {
    val factor = listOf(1.0, 10.0, 100.0, 1000.0)[decimals.coerceIn(0, 3)]
    val rounded = kotlin.math.round(this * factor) / factor
    val str = rounded.toString()
    val dotIndex = str.indexOf('.')
    return if (dotIndex < 0) {
        str
    } else {
        val intPart = str.substring(0, dotIndex)
        val decPart = str.substring(dotIndex + 1).take(decimals).padEnd(decimals, '0')
        "$intPart.$decPart"
    }
}
