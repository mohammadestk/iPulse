package dev.esteki.ipulse.domain.usecase

import dev.esteki.ipulse.domain.model.Device
import dev.esteki.ipulse.domain.repository.TelemetryRepository
import kotlinx.coroutines.flow.Flow

class ObserveTelemetryUseCase(
    private val telemetryRepository: TelemetryRepository
) {
    operator fun invoke(): Flow<List<Device>> {
        return telemetryRepository.devices
    }
}
