package dev.esteki.ipulse.data.local

import androidx.room3.Room
import org.dany.worker.createSQLiteWasmWorker

actual fun createDatabase(): AppDatabase {
    return Room.inMemoryDatabaseBuilder<AppDatabase>()
        .setDriver(createSQLiteWasmWorker())
        .build()
}