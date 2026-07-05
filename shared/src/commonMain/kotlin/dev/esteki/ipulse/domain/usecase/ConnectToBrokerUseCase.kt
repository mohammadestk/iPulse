package dev.esteki.ipulse.domain.usecase

import dev.esteki.ipulse.domain.repository.MqttRepository

class ConnectToBrokerUseCase(
    private val mqttRepository: MqttRepository
) {
    companion object {
        const val DEFAULT_BROKER = "broker.emqx.io"
        const val DEFAULT_PORT = 8084
    }

    suspend operator fun invoke(
        brokerUrl: String = DEFAULT_BROKER,
        port: Int = DEFAULT_PORT
    ) {
        mqttRepository.connect(brokerUrl, port)
    }
}
