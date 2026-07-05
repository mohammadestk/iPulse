package dev.esteki.ipulse

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import dev.esteki.ipulse.di.dataModule
import dev.esteki.ipulse.ui.screen.DashboardRoot
import dev.esteki.ipulse.ui.screen.DeviceDetailRoot
import dev.esteki.ipulse.ui.theme.Background
import dev.esteki.ipulse.ui.theme.Border
import dev.esteki.ipulse.ui.theme.DeviceChrome
import dev.esteki.ipulse.ui.theme.FaultRed
import dev.esteki.ipulse.ui.theme.Panel
import dev.esteki.ipulse.ui.theme.PanelRaised
import dev.esteki.ipulse.ui.theme.SignalAmber
import dev.esteki.ipulse.ui.theme.SignalCyan
import dev.esteki.ipulse.ui.theme.TextMuted
import dev.esteki.ipulse.ui.theme.TextPrimary
import dev.esteki.ipulse.ui.viewmodel.DashboardViewModel
import dev.esteki.ipulse.ui.viewmodel.DeviceDetailViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinConfiguration

private val DarkColorScheme = darkColorScheme(
    primary = SignalAmber,
    secondary = SignalCyan,
    tertiary = FaultRed,
    background = Background,
    surface = Panel,
    surfaceVariant = PanelRaised,
    onPrimary = DeviceChrome,
    onSecondary = DeviceChrome,
    onTertiary = DeviceChrome,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextMuted,
    outline = Border
)

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Dashboard : Route

    @Serializable
    data class DeviceDetail(val deviceId: String) : Route
}

private val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Route.Dashboard::class, Route.Dashboard.serializer())
            subclass(Route.DeviceDetail::class, Route.DeviceDetail.serializer())
        }
    }
}

@Composable
fun App() {
    KoinApplication(
        configuration = koinConfiguration(declaration = { modules(dataModule) }),
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
