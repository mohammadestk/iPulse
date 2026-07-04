package dev.esteki.ipulse.ui.model

import dev.esteki.ipulse.domain.model.Device
import dev.esteki.ipulse.domain.model.DeviceConnectionState
import dev.esteki.ipulse.domain.model.SensorType
import dev.esteki.ipulse.domain.model.TelemetryReading

data class DeviceUi(
    val id: String,
    val name: String,
    val topic: String,
    val sensorType: SensorType,
    val latestValue: String,
    val unit: String,
    val connectionState: DeviceConnectionState,
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
    isLive = connectionState == DeviceConnectionState.CONNECTED
)

private fun Double.format(decimals: Int): String {
    return "%.${decimals}f".format(this)
}
