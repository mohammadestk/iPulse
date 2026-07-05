package dev.esteki.ipulse.domain.di

import dev.esteki.ipulse.domain.usecase.ConnectToBrokerUseCase
import dev.esteki.ipulse.domain.usecase.DisconnectFromBrokerUseCase
import dev.esteki.ipulse.domain.usecase.GetDeviceByIdUseCase
import dev.esteki.ipulse.domain.usecase.ObserveConnectionEventsUseCase
import dev.esteki.ipulse.domain.usecase.ObserveConnectionStateUseCase
import dev.esteki.ipulse.domain.usecase.ObserveSignalQualityUseCase
import dev.esteki.ipulse.domain.usecase.ObserveTelemetryUseCase
import dev.esteki.ipulse.domain.usecase.SubscribeToDeviceTopicUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { ConnectToBrokerUseCase(mqttRepository = get()) }
    factory { DisconnectFromBrokerUseCase(mqttRepository = get()) }
    factory { SubscribeToDeviceTopicUseCase(mqttRepository = get()) }
    factory { ObserveTelemetryUseCase(telemetryRepository = get()) }
    factory { ObserveConnectionStateUseCase(mqttRepository = get()) }
    factory { ObserveConnectionEventsUseCase(mqttRepository = get()) }
    factory { ObserveSignalQualityUseCase(telemetryRepository = get()) }
    factory { GetDeviceByIdUseCase(telemetryRepository = get()) }
}
