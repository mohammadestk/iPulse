package dev.esteki.ipulse.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class DevicePulseColors(
    val background: Color,
    val panel: Color,
    val panelRaised: Color,
    val panelInset: Color,
    val border: Color,
    val borderSoft: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val textDim: Color,
    val signalAmber: Color,
    val signalAmberDim: Color,
    val signalCyan: Color,
    val signalCyanDim: Color,
    val faultRed: Color,
    val faultRedDim: Color,
    val deviceChrome: Color,
)

val DefaultDevicePulseColors = DevicePulseColors(
    background = Color(0xFF15181C),
    panel = Color(0xFF1D2126),
    panelRaised = Color(0xFF242A30),
    panelInset = Color(0xFF191D21),
    border = Color(0xFF333B42),
    borderSoft = Color(0xFF2A3036),
    textPrimary = Color(0xFFE9EDF0),
    textMuted = Color(0xFF8A939C),
    textDim = Color(0xFF5B646C),
    signalAmber = Color(0xFFFFB238),
    signalAmberDim = Color(0xFF6B5426),
    signalCyan = Color(0xFF45D6C4),
    signalCyanDim = Color(0xFF1F4D47),
    faultRed = Color(0xFFFF5C5C),
    faultRedDim = Color(0xFF5C2B2B),
    deviceChrome = Color(0xFF0E1013),
)
