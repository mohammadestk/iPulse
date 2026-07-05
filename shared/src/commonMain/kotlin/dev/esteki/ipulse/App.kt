package dev.esteki.ipulse

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import dev.esteki.ipulse.data.di.dataModule
import dev.esteki.ipulse.domain.di.domainModule
import dev.esteki.ipulse.presentation.di.presentationModule
import dev.esteki.ipulse.presentation.navigation.Route
import dev.esteki.ipulse.presentation.navigation.navConfig
import dev.esteki.ipulse.presentation.screen.DashboardRoot
import dev.esteki.ipulse.presentation.screen.DeviceDetailRoot
import dev.esteki.ipulse.presentation.theme.DarkColorScheme
import dev.esteki.ipulse.presentation.viewmodel.DashboardViewModel
import dev.esteki.ipulse.presentation.viewmodel.DeviceDetailViewModel
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinConfiguration

@Composable
fun App() {
    KoinApplication(
        configuration = koinConfiguration(declaration = {
            modules(dataModule, domainModule, presentationModule)
        }),
        content = {
            MaterialTheme(colorScheme = DarkColorScheme) {
                val backStack = rememberNavBackStack(navConfig, Route.Dashboard)

                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = entryProvider {
                        entry<Route.Dashboard> {
                            val viewModel: DashboardViewModel = koinInject()
                            DashboardRoot(
                                onNavigateToDeviceDetail = { deviceId ->
                                    backStack.add(Route.DeviceDetail(deviceId))
                                },
                                viewModel = viewModel
                            )
                        }
                        entry<Route.DeviceDetail> { key ->
                            val viewModel: DeviceDetailViewModel = koinInject {
                                parametersOf(key.deviceId)
                            }
                            DeviceDetailRoot(
                                onNavigateBack = { backStack.removeLastOrNull() },
                                viewModel = viewModel
                            )
                        }
                    }
                )
            }
        },
    )
}
