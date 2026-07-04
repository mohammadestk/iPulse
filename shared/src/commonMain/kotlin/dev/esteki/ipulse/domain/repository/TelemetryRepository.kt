package dev.esteki.ipulse.domain.repository

import dev.esteki.ipulse.domain.model.Device
import dev.esteki.ipulse.domain.model.SignalQuality
import dev.esteki.ipulse.domain.model.TelemetryReading
import kotlinx.coroutines.flow.Flow

interface TelemetryRepository {
    val devices: Flow<List<Device>>
    val signalQuality: Flow<SignalQuality>

    suspend fun processMessage(topic: String, payload: String, timestamp: Long)
    suspend fun getDeviceById(id: String): Device?
    suspend fun getReadingsForDevice(deviceId: String): List<TelemetryReading>
    suspend fun clearOldReadings(maxAgeMs: Long)
}
