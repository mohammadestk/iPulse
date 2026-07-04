package dev.esteki.ipulse.domain.usecase

import dev.esteki.ipulse.domain.model.ConnectionEvent
import dev.esteki.ipulse.domain.repository.MqttRepository
import kotlinx.coroutines.flow.Flow

class ObserveConnectionEventsUseCase(
    private val mqttRepository: MqttRepository
) {
    operator fun invoke(): Flow<ConnectionEvent> {
        return mqttRepository.connectionEvents
    }
}
