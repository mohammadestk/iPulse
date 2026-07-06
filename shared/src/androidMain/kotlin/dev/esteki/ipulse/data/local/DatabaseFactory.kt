package dev.esteki.ipulse.data.local

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

actual fun createDatabase(): AppDatabase {
    val context = AndroidContextHolder.context
    return Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "ipulse.db"
    ).setDriver(BundledSQLiteDriver()).build()
}
