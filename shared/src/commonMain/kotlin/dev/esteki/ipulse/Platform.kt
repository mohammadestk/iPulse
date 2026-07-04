package dev.esteki.ipulse

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform