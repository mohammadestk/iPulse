package dev.esteki.ipulse.data.service

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import dev.esteki.ipulse.data.local.dao.DeviceDao
import dev.esteki.ipulse.data.local.dao.TelemetryReadingDao
import dev.esteki.ipulse.data.local.entity.DeviceEntity
import dev.esteki.ipulse.data.local.entity.TelemetryReadingEntity
import dev.esteki.ipulse.data.model.MqttMessage
import dev.esteki.ipulse.data.remote.MqttClientBase
import dev.esteki.ipulse.domain.model.ConnectionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TelemetryIngestionServiceTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val messages = MutableSharedFlow<MqttMessage>(extraBufferCapacity = 10)
    private val mqttClient = FakeMqttClient(messages)
    private val deviceDao = InMemoryDeviceDao()
    private val readingDao = InMemoryReadingDao()
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private lateinit var service: TelemetryIngestionService

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        service = TelemetryIngestionService(mqttClient, deviceDao, readingDao, json, testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        service.close()
        Dispatchers.resetMain()
    }

    @Test
    fun processMessage_createsNewDevice() = runTest {
        service.start()
        messages.emit(MqttMessage(
            topic = "/esteki/devices",
            payload = """{"deviceId":"ward-b-bed-4","name":"Ward B","sensorType":"temperature","value":24.6,"status":"live"}"""
        ))
        testScheduler.advanceUntilIdle()

        assertTrue(deviceDao.store.containsKey("ward-b-bed-4"))
        assertEquals("Ward B", deviceDao.store["ward-b-bed-4"]?.name)
        assertEquals(1, readingDao.readings.size)
        assertEquals(24.6, readingDao.readings[0].value)
    }

    @Test
    fun processMessage_updatesExistingDevice() = runTest {
        deviceDao.upsert(DeviceEntity(
            id = "d1", name = "Old Name", topic = "/old",
            sensorType = "TEMPERATURE", connectionState = "Disconnected",
            latestReadingValue = null, latestReadingTimestamp = null, latestReadingTopic = null
        ))

        service.start()
        messages.emit(MqttMessage(
            topic = "/esteki/devices",
            payload = """{"deviceId":"d1","name":"New Name","sensorType":"humidity","value":61,"status":"live"}"""
        ))
        testScheduler.advanceUntilIdle()

        assertEquals("New Name", deviceDao.store["d1"]?.name)
        assertEquals("HUMIDITY", deviceDao.store["d1"]?.sensorType)
    }

    @Test
    fun processMessage_invalidPayload_doesNotInsert() = runTest {
        service.start()
        messages.emit(MqttMessage(topic = "/esteki/devices", payload = "not json"))
        testScheduler.advanceUntilIdle()

        assertTrue(deviceDao.store.isEmpty())
        assertTrue(readingDao.readings.isEmpty())
    }

    @Test
    fun start_isIdempotent() = runTest {
        service.start()
        service.start()

        messages.emit(MqttMessage(
            topic = "/esteki/devices",
            payload = """{"deviceId":"d1","name":"D","sensorType":"temperature","value":1,"status":"live"}"""
        ))
        testScheduler.advanceUntilIdle()

        assertEquals(1, deviceDao.store.size)
    }

    @Test
    fun close_cancelsIngestion() = runTest {
        service.start()
        service.close()

        messages.emit(MqttMessage(
            topic = "/esteki/devices",
            payload = """{"deviceId":"d1","name":"D","sensorType":"temperature","value":1,"status":"live"}"""
        ))
        testScheduler.advanceUntilIdle()

        assertTrue(deviceDao.store.isEmpty())
    }
}

private class FakeMqttClient(
    override val messages: Flow<MqttMessage>
) : MqttClientBase {
    override val connectionState: Flow<ConnectionState> = flowOf(ConnectionState.Disconnected)
    override suspend fun connect(brokerUrl: String, port: Int) {}
    override suspend fun disconnect() {}
    override suspend fun subscribe(topicFilter: String) {}
    override suspend fun unsubscribe(topicFilter: String) {}
    override suspend fun publish(topic: String, payload: String, qos: Int) {}
}

private class InMemoryDeviceDao : DeviceDao {
    val store = mutableMapOf<String, DeviceEntity>()

    override fun observeAll() = flowOf(store.values.toList())
    override suspend fun getAll() = store.values.toList()
    override suspend fun getById(id: String) = store[id]
    override fun observeById(id: String) = flowOf(store[id])
    override suspend fun upsert(device: DeviceEntity) { store[device.id] = device }
    override fun observePagingSource(): androidx.paging.PagingSource<Int, DeviceEntity> {
        throw UnsupportedOperationException()
    }
}

private class InMemoryReadingDao : TelemetryReadingDao {
    val readings = mutableListOf<TelemetryReadingEntity>()

    override fun observeByDeviceId(deviceId: String) = flowOf(
        readings.filter { it.deviceId == deviceId }.sortedByDescending { it.timestamp }
    )
    override suspend fun getByDeviceId(deviceId: String) =
        readings.filter { it.deviceId == deviceId }.sortedByDescending { it.timestamp }
    override fun observeAll() = flowOf(readings.sortedByDescending { it.timestamp })
    override suspend fun insert(reading: TelemetryReadingEntity) { readings.add(reading) }
    override suspend fun deleteOlderThan(cutoff: Long) { readings.removeAll { it.timestamp < cutoff } }
}
