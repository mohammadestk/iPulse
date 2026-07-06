package dev.esteki.ipulse.data.local

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File

actual fun createDatabase(): AppDatabase {
    val dbFile = File(System.getProperty("user.home"), ".ipulse/databases/ipulse.db")
    dbFile.parentFile?.mkdirs()
    return Room.databaseBuilder<AppDatabase>(dbFile.absolutePath)
        .setDriver(BundledSQLiteDriver())
        .build()
}
