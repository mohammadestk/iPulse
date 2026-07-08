package dev.esteki.ipulse.domain.usecase

import dev.esteki.ipulse.domain.model.Device
import dev.esteki.ipulse.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow

class ObserveDeviceById(
    private val deviceRepository: DeviceRepository
) {
    operator fun invoke(deviceId: String): Flow<Device?> = deviceRepository.observeDeviceById(deviceId)
}
