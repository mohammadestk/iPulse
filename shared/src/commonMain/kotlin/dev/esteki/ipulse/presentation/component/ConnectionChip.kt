package dev.esteki.ipulse.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.esteki.ipulse.domain.model.ConnectionState
import dev.esteki.ipulse.presentation.theme.IPulseTheme

@Composable
fun ConnectionChip(
    state: ConnectionState,
    label: String,
    modifier: Modifier = Modifier
) {
    val (color, dimColor) = when (state) {
        is ConnectionState.Connected -> IPulseTheme.colors.signalCyan to IPulseTheme.colors.signalCyanDim
        is ConnectionState.Connecting,
        is ConnectionState.Reconnecting -> IPulseTheme.colors.signalAmber to IPulseTheme.colors.signalAmberDim
        is ConnectionState.Error -> IPulseTheme.colors.faultRed to IPulseTheme.colors.faultRedDim
        is ConnectionState.Disconnected -> IPulseTheme.colors.textDim to IPulseTheme.colors.border
    }

    Row(
        modifier = modifier
            .background(dimColor.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = IPulseTheme.typography.monoMicro,
            color = color
        )
    }
}
