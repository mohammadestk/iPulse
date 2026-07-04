package dev.esteki.ipulse.domain.model

enum class SensorType(val displayName: String, val unit: String) {
    TEMPERATURE("Temperature", "°C"),
    PRESSURE("Pressure", "hPa"),
    HUMIDITY("Humidity", "%RH");

    companion object {
        fun fromTopic(topic: String): SensorType? = when {
            topic.contains("temp", ignoreCase = true) -> TEMPERATURE
            topic.contains("pressure", ignoreCase = true) -> PRESSURE
            topic.contains("humidity", ignoreCase = true) -> HUMIDITY
            else -> null
        }
    }
}
