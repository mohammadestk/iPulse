package dev.esteki.ipulse.domain.usecase

import dev.esteki.ipulse.domain.model.ConnectionState
import dev.esteki.ipulse.domain.repository.BrokerConnection
import kotlinx.coroutines.flow.Flow

class ObserveConnectionState(
    private val brokerConnection: BrokerConnection
) {
    operator fun invoke(): Flow<ConnectionState> = brokerConnection.connectionState
}
