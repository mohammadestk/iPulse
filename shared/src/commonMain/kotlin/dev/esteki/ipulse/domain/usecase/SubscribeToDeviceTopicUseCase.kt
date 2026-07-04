package dev.esteki.ipulse.domain.usecase

import dev.esteki.ipulse.domain.repository.MqttRepository

class SubscribeToDeviceTopicUseCase(
    private val mqttRepository: MqttRepository
) {
    suspend operator fun invoke(topicFilter: String) {
        mqttRepository.subscribe(topicFilter)
    }
}
