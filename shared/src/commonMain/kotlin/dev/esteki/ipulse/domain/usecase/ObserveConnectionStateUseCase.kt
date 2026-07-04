package dev.esteki.ipulse.domain.usecase

import dev.esteki.ipulse.domain.model.ConnectionState
import dev.esteki.ipulse.domain.repository.MqttRepository
import kotlinx.coroutines.flow.Flow

class ObserveConnectionStateUseCase(
    private val mqttRepository: MqttRepository
) {
    operator fun invoke(): Flow<ConnectionState> {
        return mqttRepository.connectionState
    }
}
