package dev.esteki.ipulse

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.esteki.ipulse.data.di.dataModule
import dev.esteki.ipulse.domain.di.domainModule
import dev.esteki.ipulse.presentation.di.presentationModule
import dev.esteki.ipulse.presentation.navigation.Route
import dev.esteki.ipulse.presentation.navigation.navConfig
import dev.esteki.ipulse.presentation.screen.DashboardRoot
import dev.esteki.ipulse.presentation.screen.DeviceDetailRoot
import dev.esteki.ipulse.presentation.theme.IPulseThemeContent
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinConfiguration

@Composable
fun App() {
    KoinApplication(
        configuration = koinConfiguration(
            declaration = {
                modules(dataModule, domainModule, presentationModule)
            },
        ),
        content = {
            IPulseThemeContent {
                val backStack = rememberNavBackStack(navConfig, Route.Dashboard)

                NavDisplay(
                    backStack = backStack,
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = entryProvider {
                        entry<Route.Dashboard> {
                            DashboardRoot(
                                onNavigateToDeviceDetail = { deviceId ->
                                    backStack.add(Route.DeviceDetail(deviceId))
                                },
                            )
                        }
                        entry<Route.DeviceDetail> { key ->
                            DeviceDetailRoot(
                                onNavigateBack = { backStack.removeLastOrNull() },
                                viewModel = koinViewModel { parametersOf(key.deviceId) }
                            )
                        }
                    }
                )
            }
        },
    )
}
