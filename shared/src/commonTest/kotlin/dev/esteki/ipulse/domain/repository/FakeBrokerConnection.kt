package dev.esteki.ipulse.domain.repository

import dev.esteki.ipulse.domain.model.ConnectionEvent
import dev.esteki.ipulse.domain.model.ConnectionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeBrokerConnection : BrokerConnection {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: Flow<ConnectionState> get() = _connectionState

    private val _connectionEvents = MutableSharedFlow<ConnectionEvent>(extraBufferCapacity = 10)
    override val connectionEvents: Flow<ConnectionEvent> get() = _connectionEvents

    var connectBehavior: suspend (String, Int) -> Unit = { _, _ -> }
    var disconnectBehavior: suspend () -> Unit = {}
    var subscribeBehavior: suspend (String) -> Unit = {}

    val connectCalls = mutableListOf<Pair<String, Int>>()
    val subscribeCalls = mutableListOf<String>()
    var disconnectCalled = false

    override suspend fun connect(brokerUrl: String, port: Int) {
        connectCalls.add(brokerUrl to port)
        connectBehavior(brokerUrl, port)
    }

    override suspend fun disconnect() {
        disconnectCalled = true
        disconnectBehavior()
    }

    override suspend fun subscribe(topicFilter: String) {
        subscribeCalls.add(topicFilter)
        subscribeBehavior(topicFilter)
    }

    fun setConnectionState(state: ConnectionState) {
        _connectionState.value = state
    }

    suspend fun emitConnectionEvent(event: ConnectionEvent) {
        _connectionEvents.emit(event)
    }
}
