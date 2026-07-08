package dev.esteki.ipulse.domain.repository

import dev.esteki.ipulse.domain.model.ConnectionEvent
import dev.esteki.ipulse.domain.model.ConnectionState
import kotlinx.coroutines.flow.Flow

interface BrokerConnection {
    val connectionState: Flow<ConnectionState>
    val connectionEvents: Flow<ConnectionEvent>

    suspend fun connect(brokerUrl: String, port: Int)
    suspend fun disconnect()
    suspend fun subscribe(topicFilter: String)
}
