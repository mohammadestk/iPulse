package dev.esteki.ipulse.domain.usecase

import dev.esteki.ipulse.domain.model.Device
import dev.esteki.ipulse.domain.repository.DeviceRepository

class GetDeviceById(
    private val deviceRepository: DeviceRepository
) {
    suspend operator fun invoke(deviceId: String): Result<Device> {
        return deviceRepository.getDeviceById(deviceId)
    }
}
