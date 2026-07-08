package dev.esteki.ipulse.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.esteki.ipulse.data.local.entity.DeviceEntity
import dev.esteki.ipulse.domain.model.Stability
import dev.esteki.ipulse.domain.model.ConnectionState
import dev.esteki.ipulse.domain.model.SensorType
import dev.esteki.ipulse.domain.model.TelemetryReading
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest

class DeviceRepositoryImplTest {

    // Use real in-memory DAO fakes instead of MockK (Room annotations conflict)
    private val devicesFlow = MutableStateFlow<List<DeviceEntity>>(emptyList())
    private val deviceDao = InMemoryDeviceDao(devicesFlow)
    private val readingDao = InMemoryReadingDao()
    private val repository = DeviceRepositoryImpl(deviceDao, readingDao)

    private fun entity(id: String = "d1", latestReadingTimestamp: Long? = null) = DeviceEntity(
        id = id,
        name = "Device $id",
        topic = "/t",
        sensorType = "TEMPERATURE",
        connectionState = "Connected",
        latestReadingValue = null,
        latestReadingTimestamp = latestReadingTimestamp,
        latestReadingTopic = null
    )

    // --- computeSignalQuality (tested via signalQuality flow) ---

    @Test
    fun signalQuality_emptyList_returnsNoData() = runTest {
        devicesFlow.value = emptyList()

        repository.signalQuality.test {
            val quality = awaitItem()
            assertThat(quality.stability).isEqualTo(Stability.NO_DATA)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun signalQuality_allDevicesRecent_returnsStable() = runTest {
        val now = Clock.System.now().toEpochMilliseconds()
        devicesFlow.value = (1..12).map { entity("d$it", latestReadingTimestamp = now - 1000) }

        repository.signalQuality.test {
            val quality = awaitItem()
            assertThat(quality.stability).isEqualTo(Stability.STABLE)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun signalQuality_fewDevicesRecent_returnsJittery() = runTest {
        val now = Clock.System.now().toEpochMilliseconds()
        devicesFlow.value = (1..4).map { entity("d$it", latestReadingTimestamp = now - 1000) }

        repository.signalQuality.test {
            val quality = awaitItem()
            assertThat(quality.stability).isEqualTo(Stability.JITTERY)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun signalQuality_noRecentDevices_returnsNoData() = runTest {
        val oldTimestamp = Clock.System.now().toEpochMilliseconds() - 120_000
        devicesFlow.value = (1..5).map { entity("d$it", latestReadingTimestamp = oldTimestamp) }

        repository.signalQuality.test {
            val quality = awaitItem()
            assertThat(quality.stability).isEqualTo(Stability.NO_DATA)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun signalQuality_usesLatestTimestamp() = runTest {
        val now = Clock.System.now().toEpochMilliseconds()
        devicesFlow.value = listOf(
            entity("d1", latestReadingTimestamp = now - 1000),
            entity("d2", latestReadingTimestamp = now - 2000),
            entity("d3", latestReadingTimestamp = now - 3000)
        )

        repository.signalQuality.test {
            val quality = awaitItem()
            assertThat(quality.lastReceivedAt).isNotNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- getDeviceById ---

    @Test
    fun getDeviceById_found() = runTest {
        deviceDao.upsert(entity("d1"))

        val result = repository.getDeviceById("d1")

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()?.id).isEqualTo("d1")
    }

    @Test
    fun getDeviceById_notFound() = runTest {
        val result = repository.getDeviceById("missing")

        assertThat(result.isFailure).isTrue()
    }

    // --- devices flow ---

    @Test
    fun devices_mapsEntitiesToDomain() = runTest {
        devicesFlow.value = listOf(entity("d1"), entity("d2"))

        repository.devices.test {
            val devices = awaitItem()
            assertThat(devices).hasSize(2)
            assertThat(devices[0].id).isEqualTo("d1")
            assertThat(devices[1].id).isEqualTo("d2")
            cancelAndIgnoreRemainingEvents()
        }
    }
}

// Simple in-memory DAO fakes to avoid MockK/Room annotation conflicts
private class InMemoryDeviceDao(
    private val devicesFlow: MutableStateFlow<List<DeviceEntity>>
) : dev.esteki.ipulse.data.local.dao.DeviceDao {
    private val store = mutableMapOf<String, DeviceEntity>()

    override fun observeAll() = devicesFlow

    override suspend fun getAll() = store.values.toList()

    override suspend fun getById(id: String) = store[id]

    override fun observeById(id: String) = kotlinx.coroutines.flow.flowOf(store[id])

    override suspend fun upsert(device: DeviceEntity) {
        store[device.id] = device
        devicesFlow.value = store.values.toList()
    }

    override fun observePagingSource(): androidx.paging.PagingSource<Int, DeviceEntity> {
        throw UnsupportedOperationException("Not needed in unit tests")
    }
}

private class InMemoryReadingDao : dev.esteki.ipulse.data.local.dao.TelemetryReadingDao {
    private val readings = mutableListOf<dev.esteki.ipulse.data.local.entity.TelemetryReadingEntity>()

    override fun observeByDeviceId(deviceId: String) = kotlinx.coroutines.flow.flowOf(
        readings.filter { it.deviceId == deviceId }.sortedByDescending { it.timestamp }
    )

    override suspend fun getByDeviceId(deviceId: String) =
        readings.filter { it.deviceId == deviceId }.sortedByDescending { it.timestamp }

    override fun observeAll() = kotlinx.coroutines.flow.flowOf(readings.sortedByDescending { it.timestamp })

    override suspend fun insert(reading: dev.esteki.ipulse.data.local.entity.TelemetryReadingEntity) {
        readings.add(reading)
    }

    override suspend fun deleteOlderThan(cutoff: Long) {
        readings.removeAll { it.timestamp < cutoff }
    }
}
