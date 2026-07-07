package dev.esteki.ipulse.domain.usecase

import dev.esteki.ipulse.domain.model.DomainError
import dev.esteki.ipulse.domain.repository.BrokerConnection

class DisconnectFromBroker(
    private val brokerConnection: BrokerConnection
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            brokerConnection.disconnect()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(DomainError.Connection("Failed to disconnect", e))
        }
    }
}
