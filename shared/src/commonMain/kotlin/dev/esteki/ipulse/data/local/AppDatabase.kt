package dev.esteki.ipulse.data.local

import androidx.room3.Database
import androidx.room3.RoomDatabase
import dev.esteki.ipulse.data.local.dao.DeviceDao
import dev.esteki.ipulse.data.local.dao.TelemetryReadingDao
import dev.esteki.ipulse.data.local.entity.DeviceEntity
import dev.esteki.ipulse.data.local.entity.TelemetryReadingEntity

@Database(
    entities = [DeviceEntity::class, TelemetryReadingEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun telemetryReadingDao(): TelemetryReadingDao
}
