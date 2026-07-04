package dev.esteki.ipulse

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import dev.esteki.ipulse.di.dataModule
import dev.esteki.ipulse.ui.screen.DashboardRoot
import dev.esteki.ipulse.ui.screen.DeviceDetailRoot
import dev.esteki.ipulse.ui.theme.*
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

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

@Composable
fun App() {
    KoinApplication(
        application = {
            modules(dataModule)
        }
    ) {
        MaterialTheme(colorScheme = DarkColorScheme) {
            var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

            when (val screen = currentScreen) {
                is Screen.Dashboard -> {
                    val viewModel: dev.esteki.ipulse.ui.viewmodel.DashboardViewModel = koinInject()
                    DashboardRoot(
                        onNavigateToDeviceDetail = { deviceId ->
                            currentScreen = Screen.DeviceDetail(deviceId)
                        },
                        viewModel = viewModel
                    )
                }
                is Screen.DeviceDetail -> {
                    val viewModel: dev.esteki.ipulse.ui.viewmodel.DeviceDetailViewModel = koinInject {
                        parametersOf(screen.deviceId)
                    }
                    DeviceDetailRoot(
                        onNavigateBack = { currentScreen = Screen.Dashboard },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

sealed class Screen {
    data object Dashboard : Screen()
    data class DeviceDetail(val deviceId: String) : Screen()
}
