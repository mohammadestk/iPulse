package dev.esteki.ipulse.domain.usecase

import dev.esteki.ipulse.domain.model.DomainError
import dev.esteki.ipulse.domain.repository.BrokerConnection

class ConnectToBroker(
    private val brokerConnection: BrokerConnection
) {
    suspend operator fun invoke(brokerUrl: String, port: Int): Result<Unit> {
        return try {
            brokerConnection.connect(brokerUrl, port)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(DomainError.Connection("Failed to connect to $brokerUrl:$port", e))
        }
    }
}
