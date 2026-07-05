package dev.esteki.ipulse.domain.usecase

import dev.esteki.ipulse.domain.model.Device
import dev.esteki.ipulse.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow

class ObserveTelemetry(
    private val deviceRepository: DeviceRepository
) {
    operator fun invoke(): Flow<List<Device>> = deviceRepository.devices
}
