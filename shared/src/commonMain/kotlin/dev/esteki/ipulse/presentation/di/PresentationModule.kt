package dev.esteki.ipulse.presentation.di

import dev.esteki.ipulse.presentation.viewmodel.DashboardViewModel
import dev.esteki.ipulse.presentation.viewmodel.DeviceDetailViewModel
import org.koin.dsl.module

val presentationModule = module {
    factory {
        DashboardViewModel(
            connectToBroker = get(),
            subscribeToDeviceTopic = get(),
            observeTelemetry = get(),
            observeConnectionState = get(),
            observeConnectionEvents = get(),
            observeSignalQuality = get()
        )
    }

    factory { params ->
        DeviceDetailViewModel(
            deviceId = params.get(),
            getDeviceById = get(),
            observeConnectionEvents = get(),
            observeSignalQuality = get()
        )
    }
}
