package dev.esteki.ipulse.domain.usecase

import dev.esteki.ipulse.domain.repository.BrokerConnection

class ConnectToBroker(
    private val brokerConnection: BrokerConnection
) {
    suspend operator fun invoke(brokerUrl: String, port: Int) {
        brokerConnection.connect(brokerUrl, port)
    }
}
