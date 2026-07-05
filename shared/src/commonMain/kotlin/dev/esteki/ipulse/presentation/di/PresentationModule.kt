package dev.esteki.ipulse.presentation.di

import dev.esteki.ipulse.presentation.viewmodel.DashboardViewModel
import dev.esteki.ipulse.presentation.viewmodel.DeviceDetailViewModel
import org.koin.dsl.module

val presentationModule = module {
    factory {
        DashboardViewModel(
            connectToBroker = get(),
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
