package dev.esteki.ipulse.data.di

import dev.esteki.ipulse.data.remote.KempMqttClient
import dev.esteki.ipulse.data.remote.MqttClientAdapter
import dev.esteki.ipulse.data.repository.BrokerConnectionImpl
import dev.esteki.ipulse.data.repository.DeviceRepositoryImpl
import dev.esteki.ipulse.domain.repository.BrokerConnection
import dev.esteki.ipulse.domain.repository.DeviceRepository
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

    single<BrokerConnection> {
        BrokerConnectionImpl(mqttClient = get())
    }

    single<DeviceRepository> {
        DeviceRepositoryImpl(mqttClient = get(), json = get())
    }
}
