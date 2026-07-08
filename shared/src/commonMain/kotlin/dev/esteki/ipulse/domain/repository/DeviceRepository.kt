package dev.esteki.ipulse.domain.repository

import androidx.paging.PagingData
import dev.esteki.ipulse.domain.model.Device
import dev.esteki.ipulse.domain.model.SignalQuality
import dev.esteki.ipulse.domain.model.TelemetryReading
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    val devices: Flow<List<Device>>
    val signalQuality: Flow<SignalQuality>

    fun observeDeviceById(id: String): Flow<Device?>
    suspend fun getDeviceById(id: String): Result<Device>
    suspend fun getReadingsForDevice(deviceId: String): Result<List<TelemetryReading>>
    fun observeDevicesPaged(): Flow<PagingData<Device>>
}
