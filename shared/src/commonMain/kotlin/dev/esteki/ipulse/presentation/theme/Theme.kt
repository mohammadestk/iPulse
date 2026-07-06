package dev.esteki.ipulse.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalDevicePulseTypography = staticCompositionLocalOf<DevicePulseTypography> {
    error("No DevicePulseTypography provided")
}

private val LocalDevicePulseColors = staticCompositionLocalOf<DevicePulseColors> {
    error("No DevicePulseColors provided")
}

object IPulseTheme {
    val typography: DevicePulseTypography
        @Composable
        get() = LocalDevicePulseTypography.current

    val colors: DevicePulseColors
        @Composable
        get() = LocalDevicePulseColors.current
}

@Composable
fun IPulseThemeContent(content: @Composable () -> Unit) {
    val typography = createDevicePulseTypography()

    CompositionLocalProvider(
        LocalDevicePulseTypography provides typography,
        LocalDevicePulseColors provides DefaultDevicePulseColors
    ) {
        MaterialTheme(colorScheme = DarkColorScheme) {
            content()
        }
    }
}

private val DarkColorScheme = darkColorScheme(
    primary = DefaultDevicePulseColors.signalAmber,
    secondary = DefaultDevicePulseColors.signalCyan,
    tertiary = DefaultDevicePulseColors.faultRed,
    background = DefaultDevicePulseColors.background,
    surface = DefaultDevicePulseColors.panel,
    surfaceVariant = DefaultDevicePulseColors.panelRaised,
    onPrimary = DefaultDevicePulseColors.deviceChrome,
    onSecondary = DefaultDevicePulseColors.deviceChrome,
    onTertiary = DefaultDevicePulseColors.deviceChrome,
    onBackground = DefaultDevicePulseColors.textPrimary,
    onSurface = DefaultDevicePulseColors.textPrimary,
    onSurfaceVariant = DefaultDevicePulseColors.textMuted,
    outline = DefaultDevicePulseColors.border
)
