package dev.esteki.ipulse.domain.usecase

import dev.esteki.ipulse.domain.model.ConnectionEvent
import dev.esteki.ipulse.domain.repository.BrokerConnection
import kotlinx.coroutines.flow.Flow

class ObserveConnectionEvents(
    private val brokerConnection: BrokerConnection
) {
    operator fun invoke(): Flow<ConnectionEvent> = brokerConnection.connectionEvents
}
