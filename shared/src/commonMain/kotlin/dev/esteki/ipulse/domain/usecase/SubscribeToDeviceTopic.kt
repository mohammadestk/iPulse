package dev.esteki.ipulse.domain.usecase

import dev.esteki.ipulse.domain.repository.BrokerConnection

class SubscribeToDeviceTopic(
    private val brokerConnection: BrokerConnection
) {
    suspend operator fun invoke(topicFilter: String) {
        brokerConnection.subscribe(topicFilter)
    }
}
