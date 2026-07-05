package dev.esteki.ipulse.domain.usecase

import dev.esteki.ipulse.domain.repository.MqttRepository

class ConnectToBrokerUseCase(
    private val mqttRepository: MqttRepository
) {
    companion object {
        const val EMQX_BROKER = "broker.emqx.io"
        const val EMQX_PORT = 8084
        const val MOSQUITTO_BROKER = "test.mosquitto.org"
        const val MOSQUITTO_PORT = 8081
    }

    suspend operator fun invoke(
        brokerUrl: String = MOSQUITTO_BROKER,
        port: Int = MOSQUITTO_PORT
    ) {
        mqttRepository.connect(brokerUrl, port)
    }
}
