package dev.esteki.ipulse.domain.usecase

import dev.esteki.ipulse.domain.model.SignalQuality
import dev.esteki.ipulse.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow

class ObserveSignalQuality(
    private val deviceRepository: DeviceRepository
) {
    operator fun invoke(): Flow<SignalQuality> = deviceRepository.signalQuality
}
