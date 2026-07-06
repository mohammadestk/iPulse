package dev.esteki.ipulse.data.local

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import dev.esteki.ipulse.data.local.dao.DeviceDao
import dev.esteki.ipulse.data.local.dao.TelemetryReadingDao
import dev.esteki.ipulse.data.local.entity.DeviceEntity
import dev.esteki.ipulse.data.local.entity.TelemetryReadingEntity

@Database(
    entities = [DeviceEntity::class, TelemetryReadingEntity::class],
    version = 1,
    exportSchema = true
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun telemetryReadingDao(): TelemetryReadingDao
}

@Suppress("KotlinNoActualForExpected", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

expect fun databaseBuilder(): RoomDatabase.Builder<AppDatabase>