package dev.esteki.ipulse.data.repository

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

    override suspend fun getReadingsForDevice(deviceId: String): Result<List<TelemetryReading>> {
        return try {
            val readings = readingDao.getByDeviceId(deviceId).map { it.toDomain() }
            Result.success(readings)
        } catch (e: Exception) {
            Result.failure(DomainError.Unknown(e))
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
