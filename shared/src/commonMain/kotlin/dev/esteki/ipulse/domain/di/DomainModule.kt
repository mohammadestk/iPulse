package dev.esteki.ipulse.domain.di

import dev.esteki.ipulse.domain.usecase.*
import org.koin.dsl.module

val domainModule = module {
    factory { ConnectToBroker(brokerConnection = get()) }
    factory { DisconnectFromBroker(brokerConnection = get()) }
    factory { SubscribeToDeviceTopic(brokerConnection = get()) }
    factory { ObserveTelemetry(deviceRepository = get()) }
    factory { ObserveConnectionState(brokerConnection = get()) }
    factory { ObserveConnectionEvents(brokerConnection = get()) }
    factory { ObserveSignalQuality(deviceRepository = get()) }
    factory { GetDeviceById(deviceRepository = get()) }
    factory { GetDeviceReadings(deviceRepository = get()) }
}
