package dev.esteki.ipulse.domain.usecase

import dev.esteki.ipulse.domain.model.SignalQuality
import dev.esteki.ipulse.domain.repository.TelemetryRepository
import kotlinx.coroutines.flow.Flow

class ObserveSignalQualityUseCase(
    private val telemetryRepository: TelemetryRepository
) {
    operator fun invoke(): Flow<SignalQuality> {
        return telemetryRepository.signalQuality
    }
}
