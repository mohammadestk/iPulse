package dev.esteki.ipulse.di

import dev.esteki.ipulse.data.remote.KtorMqttClient
import dev.esteki.ipulse.data.remote.MqttClient
import dev.esteki.ipulse.data.repository.MqttRepositoryImpl
import dev.esteki.ipulse.data.repository.TelemetryRepositoryImpl
import dev.esteki.ipulse.domain.repository.MqttRepository
import dev.esteki.ipulse.domain.repository.TelemetryRepository
import dev.esteki.ipulse.domain.usecase.*
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val dataModule = module {
    single<Json> {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    single<HttpClient> {
        HttpClient {
            install(WebSockets)
            install(ContentNegotiation) {
                json(get())
            }
        }
    }

    single<MqttClient> {
        KtorMqttClient(httpClient = get())
    }

    single<MqttRepository> {
        MqttRepositoryImpl(mqttClient = get())
    }

    single<TelemetryRepository> {
        TelemetryRepositoryImpl(mqttClient = get(), json = get())
    }

    factory { ConnectToBrokerUseCase(mqttRepository = get()) }
    factory { DisconnectFromBrokerUseCase(mqttRepository = get()) }
    factory { SubscribeToDeviceTopicUseCase(mqttRepository = get()) }
    factory { ObserveTelemetryUseCase(telemetryRepository = get()) }
    factory { ObserveConnectionStateUseCase(mqttRepository = get()) }
    factory { ObserveConnectionEventsUseCase(mqttRepository = get()) }
    factory { ObserveSignalQualityUseCase(telemetryRepository = get()) }
    factory { GetDeviceByIdUseCase(telemetryRepository = get()) }
}
