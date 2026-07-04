package dev.esteki.ipulse.domain.usecase

import dev.esteki.ipulse.domain.repository.MqttRepository

class DisconnectFromBrokerUseCase(
    private val mqttRepository: MqttRepository
) {
    suspend operator fun invoke() {
        mqttRepository.disconnect()
    }
}
