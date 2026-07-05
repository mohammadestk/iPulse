package dev.esteki.ipulse.data.di

import dev.esteki.ipulse.data.remote.KempMqttClient
import dev.esteki.ipulse.data.remote.MqttClientAdapter
import dev.esteki.ipulse.data.repository.MqttRepositoryImpl
import dev.esteki.ipulse.data.repository.TelemetryRepositoryImpl
import dev.esteki.ipulse.domain.repository.MqttRepository
import dev.esteki.ipulse.domain.repository.TelemetryRepository
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val dataModule = module {
    single<Json> {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    single<MqttClientAdapter> {
        KempMqttClient()
    }

    single<MqttRepository> {
        MqttRepositoryImpl(mqttClient = get())
    }

    single<TelemetryRepository> {
        TelemetryRepositoryImpl(mqttClient = get(), json = get())
    }
}
