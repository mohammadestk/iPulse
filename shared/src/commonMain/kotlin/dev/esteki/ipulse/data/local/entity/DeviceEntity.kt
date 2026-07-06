package dev.esteki.ipulse.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val topic: String,
    val sensorType: String,
    val latestReadingValue: Double?,
    val latestReadingTimestamp: Long?,
    val latestReadingTopic: String?
)
