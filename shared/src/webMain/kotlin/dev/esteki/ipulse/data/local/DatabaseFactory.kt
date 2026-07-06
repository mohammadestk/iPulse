package dev.esteki.ipulse.data.local

import androidx.room3.Room
import androidx.room3.RoomDatabase
import dev.esteki.ipulse.worker.createSQLiteWasmWorker
import dev.esteki.ipulse.worker.createSqlJsWorker
import kotlinx.coroutines.Dispatchers

actual fun databaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    return Room.databaseBuilder<AppDatabase>(name = "app_database.db")
        .setDriver(createSQLiteWasmWorker())
}