package dev.esteki.ipulse.di

import dev.esteki.ipulse.data.remote.KtorMqttClient
import dev.esteki.ipulse.data.remote.MqttClient
import dev.esteki.ipulse.data.repository.MqttRepositoryImpl
import dev.esteki.ipulse.data.repository.TelemetryRepositoryImpl
import dev.esteki.ipulse.domain.repository.MqttRepository
import dev.esteki.ipulse.domain.repository.TelemetryRepository
import dev.esteki.ipulse.domain.usecase.ConnectToBrokerUseCase
import dev.esteki.ipulse.domain.usecase.DisconnectFromBrokerUseCase
import dev.esteki.ipulse.domain.usecase.GetDeviceByIdUseCase
import dev.esteki.ipulse.domain.usecase.ObserveConnectionEventsUseCase
import dev.esteki.ipulse.domain.usecase.ObserveConnectionStateUseCase
import dev.esteki.ipulse.domain.usecase.ObserveSignalQualityUseCase
import dev.esteki.ipulse.domain.usecase.ObserveTelemetryUseCase
import dev.esteki.ipulse.domain.usecase.SubscribeToDeviceTopicUseCase
import dev.esteki.ipulse.ui.viewmodel.DashboardViewModel
import dev.esteki.ipulse.ui.viewmodel.DeviceDetailViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
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
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
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

    factory {
        DashboardViewModel(
            connectToBroker = get(),
            disconnectFromBroker = get(),
            subscribeToDeviceTopic = get(),
            observeTelemetryUseCase = get(),
            observeConnectionStateUseCase = get(),
            observeConnectionEventsUseCase = get(),
            observeSignalQualityUseCase = get()
        )
    }

    factory { params ->
        DeviceDetailViewModel(
            deviceId = params.get(),
            getDeviceById = get(),
            observeConnectionEventsUseCase = get(),
            observeSignalQualityUseCase = get()
        )
    }
}
