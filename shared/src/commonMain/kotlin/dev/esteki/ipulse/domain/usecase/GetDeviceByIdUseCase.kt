package dev.esteki.ipulse.domain.usecase

import dev.esteki.ipulse.domain.model.Device
import dev.esteki.ipulse.domain.repository.TelemetryRepository

class GetDeviceByIdUseCase(
    private val telemetryRepository: TelemetryRepository
) {
    suspend operator fun invoke(deviceId: String): Device? {
        return telemetryRepository.getDeviceById(deviceId)
    }
}
