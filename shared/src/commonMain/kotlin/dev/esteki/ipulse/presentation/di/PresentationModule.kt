package dev.esteki.ipulse.presentation.di

import dev.esteki.ipulse.domain.model.ConnectionState
import dev.esteki.ipulse.presentation.model.ConnectionStateUi
import dev.esteki.ipulse.presentation.screen.DashboardState
import dev.esteki.ipulse.presentation.screen.DeviceDetailState
import dev.esteki.ipulse.presentation.viewmodel.DashboardViewModel
import dev.esteki.ipulse.presentation.viewmodel.DeviceDetailViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.plugin.module.dsl.viewModel

@OptIn(KoinExperimentalAPI::class)
val presentationModule = module {
    factory {
        DashboardState(
            devices = emptyList(),
            connectionState = ConnectionStateUi(
                state = ConnectionState.Disconnected,
                displayName = "Disconnected",
                isConnected = false
            ),
            signalQuality = null,
            connectionEvents = emptyList(),
            isLoading = false,
            searchQuery = "",
            errorMessage = null
        )
    }

    factory {
        DeviceDetailState(
            device = null,
            signalQuality = null,
            connectionEvents = emptyList(),
            readings = emptyList(),
            isLoading = false,
            errorMessage = null
        )
    }

    viewModel<DashboardViewModel>()
    viewModel<DeviceDetailViewModel>()
}
