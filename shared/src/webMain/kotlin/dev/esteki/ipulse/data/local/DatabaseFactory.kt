package dev.esteki.ipulse.data.local

import androidx.room3.Room
import androidx.sqlite.driver.web.WebSQLiteDriver

actual fun createDatabase(): AppDatabase {
    return Room.databaseBuilder<AppDatabase>("ipulse.db")
        .setDriver(WebSQLiteDriver())
        .build()
}
