package dev.esteki.ipulse.domain.usecase

import dev.esteki.ipulse.domain.model.DomainError
import dev.esteki.ipulse.domain.repository.BrokerConnection

class SubscribeToDeviceTopic(
    private val brokerConnection: BrokerConnection
) {
    suspend operator fun invoke(topicFilter: String): Result<Unit> {
        return try {
            brokerConnection.subscribe(topicFilter)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(DomainError.Subscription(topicFilter, e))
        }
    }
}
