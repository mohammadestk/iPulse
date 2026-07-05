package dev.esteki.ipulse.domain.usecase

import dev.esteki.ipulse.domain.model.TelemetryReading
import dev.esteki.ipulse.domain.repository.DeviceRepository

class GetDeviceReadings(
    private val deviceRepository: DeviceRepository
) {
    suspend operator fun invoke(deviceId: String): List<TelemetryReading> {
        return deviceRepository.getReadingsForDevice(deviceId)
    }
}
