package dev.esteki.ipulse.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import dev.esteki.ipulse.data.local.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices")
    fun observeAll(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices")
    suspend fun getAll(): List<DeviceEntity>

    @Query("SELECT * FROM devices WHERE id = :id")
    suspend fun getById(id: String): DeviceEntity?

    @Query("SELECT * FROM devices WHERE id = :id")
    fun observeById(id: String): Flow<DeviceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(device: DeviceEntity)
}
