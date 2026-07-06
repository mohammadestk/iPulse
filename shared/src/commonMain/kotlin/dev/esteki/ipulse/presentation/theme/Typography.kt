package dev.esteki.ipulse.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ipulse.shared.generated.resources.IBMPlexMono_Bold
import ipulse.shared.generated.resources.IBMPlexMono_Light
import ipulse.shared.generated.resources.IBMPlexMono_Medium
import ipulse.shared.generated.resources.IBMPlexMono_Regular
import ipulse.shared.generated.resources.IBMPlexMono_SemiBold
import ipulse.shared.generated.resources.Res
import org.jetbrains.compose.resources.Font

@Immutable
data class DevicePulseTypography(
    val display: TextStyle,
    val title: TextStyle,
    val subtitle: TextStyle,
    val body: TextStyle,
    val bodySmall: TextStyle,
    val caption: TextStyle,
    val overline: TextStyle,
    val dataLarge: TextStyle,
    val dataMedium: TextStyle,
    val dataSmall: TextStyle,
    val monoMicro: TextStyle,
    val buttonLabel: TextStyle,
)

@Composable
internal fun createDevicePulseTypography(): DevicePulseTypography {
    val fontFamily = FontFamily(
        Font(resource = Res.font.IBMPlexMono_Light, weight = FontWeight.Light),
        Font(resource = Res.font.IBMPlexMono_Regular, weight = FontWeight.Normal),
        Font(resource = Res.font.IBMPlexMono_Medium, weight = FontWeight.Medium),
        Font(resource = Res.font.IBMPlexMono_SemiBold, weight = FontWeight.SemiBold),
        Font(resource = Res.font.IBMPlexMono_Bold, weight = FontWeight.Bold),
    )

    return DevicePulseTypography(
        display = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 44.sp,
            lineHeight = 48.sp
        ),
        title = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 24.sp,
            lineHeight = 29.sp
        ),
        subtitle = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            lineHeight = 22.sp
        ),
        body = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 26.sp
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.5.sp,
            lineHeight = 19.sp
        ),
        caption = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.5.sp,
            lineHeight = 19.sp
        ),
        overline = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 11.sp,
            letterSpacing = 0.08.sp
        ),
        dataLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 34.sp,
            lineHeight = 34.sp
        ),
        dataMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
            lineHeight = 22.sp
        ),
        dataSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 18.sp
        ),
        monoMicro = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            lineHeight = 15.sp
        ),
        buttonLabel = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.5.sp,
            lineHeight = 12.5.sp,
            letterSpacing = 0.04.sp
        ),
    )
}
