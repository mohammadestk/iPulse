package dev.esteki.ipulse.domain.usecase

import dev.esteki.ipulse.domain.model.TelemetryReading
import dev.esteki.ipulse.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow

class GetDeviceReadings(
    private val deviceRepository: DeviceRepository
) {
    operator fun invoke(deviceId: String): Flow<List<TelemetryReading>> {
        return deviceRepository.observeReadingsForDevice(deviceId)
    }
}
