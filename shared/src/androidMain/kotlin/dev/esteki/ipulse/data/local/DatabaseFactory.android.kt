package dev.esteki.ipulse.data.local

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

object AndroidContextHolder {
    lateinit var appContext: Context
}

actual fun databaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val ctx = AndroidContextHolder.appContext
    return Room.databaseBuilder<AppDatabase>(name = ctx.getDatabasePath("app_databases.db").absolutePath)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
}