package dev.esteki.ipulse.domain.usecase

import dev.esteki.ipulse.domain.repository.BrokerConnection

class DisconnectFromBroker(
    private val brokerConnection: BrokerConnection
) {
    suspend operator fun invoke() {
        brokerConnection.disconnect()
    }
}
