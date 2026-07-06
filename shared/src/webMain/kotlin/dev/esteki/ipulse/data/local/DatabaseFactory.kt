package dev.esteki.ipulse.data.local

import androidx.room3.Room
import androidx.room3.RoomDatabase
import dev.esteki.ipulse.worker.createSQLiteWasmWorker

actual fun databaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    return Room.inMemoryDatabaseBuilder<AppDatabase>()
        .setDriver(createSQLiteWasmWorker())
}