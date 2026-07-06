package dev.esteki.ipulse.data.local

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual fun createDatabase(): AppDatabase {
    val fileManager = NSFileManager.defaultManager
    val directory = fileManager.URLsForDirectory(
        NSDocumentDirectory,
        inDomains = NSUserDomainMask
    ).first() as platform.Foundation.NSURL

    val dbPath = directory.path?.plus("/ipulse.db") ?: error("Could not get database path")

    return Room.databaseBuilder<AppDatabase>(dbPath)
        .setDriver(BundledSQLiteDriver())
        .build()
}
