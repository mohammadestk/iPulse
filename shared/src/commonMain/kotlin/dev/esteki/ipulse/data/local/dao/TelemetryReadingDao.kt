package dev.esteki.ipulse.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import dev.esteki.ipulse.data.local.entity.TelemetryReadingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TelemetryReadingDao {
    @Query("SELECT * FROM telemetry_readings WHERE deviceId = :deviceId ORDER BY timestamp DESC")
    fun observeByDeviceId(deviceId: String): Flow<List<TelemetryReadingEntity>>

    @Query("SELECT * FROM telemetry_readings WHERE deviceId = :deviceId ORDER BY timestamp DESC")
    suspend fun getByDeviceId(deviceId: String): List<TelemetryReadingEntity>

    @Query("SELECT * FROM telemetry_readings ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<TelemetryReadingEntity>>

    @Insert
    suspend fun insert(reading: TelemetryReadingEntity)

    @Query("DELETE FROM telemetry_readings WHERE timestamp < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)
}
