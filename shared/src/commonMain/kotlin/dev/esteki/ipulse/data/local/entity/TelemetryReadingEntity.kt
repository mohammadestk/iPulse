package dev.esteki.ipulse.data.local.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "telemetry_readings",
    foreignKeys = [ForeignKey(
        entity = DeviceEntity::class,
        parentColumns = ["id"],
        childColumns = ["deviceId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("deviceId"), Index("timestamp")]
)
data class TelemetryReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceId: String,
    val value: Double,
    val sensorType: String,
    val timestamp: Long,
    val topic: String
)
