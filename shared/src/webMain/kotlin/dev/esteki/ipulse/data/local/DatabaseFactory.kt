package dev.esteki.ipulse.data.local

import androidx.room3.Room
import dev.esteki.ipulse.worker.createSQLiteWasmWorker

actual fun createDatabase(): AppDatabase {
    return Room.inMemoryDatabaseBuilder<AppDatabase>()
        .setDriver(createSQLiteWasmWorker())
        .build()
}