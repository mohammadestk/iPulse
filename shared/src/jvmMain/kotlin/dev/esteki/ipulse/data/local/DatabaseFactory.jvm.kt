package dev.esteki.ipulse.data.local

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

actual fun createDatabase(): AppDatabase {
    return Room.databaseBuilder<AppDatabase>(name = "app_database.db")
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}