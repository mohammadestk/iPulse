package dev.esteki.ipulse.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import dev.esteki.ipulse.data.local.dao.DeviceDao
import dev.esteki.ipulse.data.local.dao.TelemetryReadingDao
import dev.esteki.ipulse.data.local.mapper.toDomain
import dev.esteki.ipulse.domain.model.Device
import dev.esteki.ipulse.domain.model.DomainError
import dev.esteki.ipulse.domain.model.SignalQuality
import dev.esteki.ipulse.domain.model.Stability
import dev.esteki.ipulse.domain.model.TelemetryReading
import dev.esteki.ipulse.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.Instant

class DeviceRepositoryImpl(
    private val deviceDao: DeviceDao,
    private val readingDao: TelemetryReadingDao
) : DeviceRepository {

    override val devices: Flow<List<Device>> = deviceDao.observeAll().map { entities ->
        entities.map { it.toDomain() }
    }

    override val signalQuality: Flow<SignalQuality> = deviceDao.observeAll().map { entities ->
        computeSignalQuality(entities)
    }

    override fun observeDeviceById(id: String): Flow<Device?> = deviceDao.observeById(id).map { entity ->
        entity?.toDomain()
    }

    override suspend fun getDeviceById(id: String): Result<Device> {
        return try {
            val entity = deviceDao.getById(id)
            if (entity != null) {
                Result.success(entity.toDomain())
            } else {
                Result.failure(DomainError.DeviceNotFound(id))
            }
        } catch (e: Exception) {
            Result.failure(DomainError.Unknown(e))
        }
    }

    override fun observeReadingsForDevice(deviceId: String): Flow<List<TelemetryReading>> {
        return readingDao.observeByDeviceId(deviceId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeDevicesPaged(): Flow<PagingData<Device>> {
        return Pager(
            config = PagingConfig(pageSize = 25)
        ) {
            deviceDao.observePagingSource()
        }.flow.map { pagingData ->
            pagingData.map { entity -> entity.toDomain() }
        }
    }

    private fun computeSignalQuality(entities: List<dev.esteki.ipulse.data.local.entity.DeviceEntity>): SignalQuality {
        if (entities.isEmpty()) {
            return SignalQuality(0.0, Stability.NO_DATA)
        }

        val now = Clock.System.now().toEpochMilliseconds()
        val recentCount = entities.count { device ->
            val timestamp = device.latestReadingTimestamp ?: 0
            timestamp > now - 60000
        }

        val stability = when {
            recentCount > 10 -> Stability.STABLE
            recentCount > 3 -> Stability.JITTERY
            else -> Stability.NO_DATA
        }

        val lastTimestamp = entities
            .mapNotNull { it.latestReadingTimestamp }
            .maxByOrNull { it } ?: 0L

        return SignalQuality(
            averageLatencyMs = 40.0,
            stability = stability,
            lastReceivedAt = if (lastTimestamp > 0) Instant.fromEpochMilliseconds(lastTimestamp) else null
        )
    }
}
