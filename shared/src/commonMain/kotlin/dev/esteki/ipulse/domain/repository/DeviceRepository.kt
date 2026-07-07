package dev.esteki.ipulse.domain.repository

import dev.esteki.ipulse.domain.model.Device
import dev.esteki.ipulse.domain.model.SignalQuality
import dev.esteki.ipulse.domain.model.TelemetryReading
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    val devices: Flow<List<Device>>
    val signalQuality: Flow<SignalQuality>

    suspend fun getDeviceById(id: String): Result<Device>
    suspend fun getReadingsForDevice(deviceId: String): Result<List<TelemetryReading>>
}
