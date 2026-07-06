package dev.esteki.ipulse.presentation.di

import dev.esteki.ipulse.presentation.viewmodel.DashboardViewModel
import dev.esteki.ipulse.presentation.viewmodel.DeviceDetailViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.plugin.module.dsl.viewModel

@OptIn(KoinExperimentalAPI::class)
val presentationModule = module {
    viewModel<DashboardViewModel>()
    viewModel<DeviceDetailViewModel>()
}
