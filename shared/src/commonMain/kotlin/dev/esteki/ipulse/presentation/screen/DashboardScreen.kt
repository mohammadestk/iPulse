package dev.esteki.ipulse.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.esteki.ipulse.domain.model.ConnectionState
import dev.esteki.ipulse.presentation.model.ConnectionStateUi
import dev.esteki.ipulse.presentation.model.DeviceUi
import dev.esteki.ipulse.presentation.model.SignalQualityUi
import dev.esteki.ipulse.presentation.theme.IPulseTheme
import dev.esteki.ipulse.presentation.viewmodel.DashboardViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardRoot(
    onNavigateToDeviceDetail: (String) -> Unit,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DashboardEvent.NavigateToDeviceDetail -> onNavigateToDeviceDetail(event.deviceId)
                is DashboardEvent.ShowError -> { /* Handle error */
                }
            }
        }
    }

    DashboardScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun DashboardScreen(
    state: DashboardState,
    onAction: (DashboardAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IPulseTheme.colors.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Sensors",
            style = IPulseTheme.typography.title,
            color = IPulseTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "broker: broker.emqx.io",
            style = IPulseTheme.typography.monoMicro,
            color = IPulseTheme.colors.textDim
        )

        Spacer(modifier = Modifier.height(12.dp))

        ConnectionStatusBar(
            connectionState = state.connectionState,
            signalQuality = state.signalQuality
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { onAction(DashboardAction.OnSearchQueryChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "filter by topic or name",
                    style = IPulseTheme.typography.monoMicro,
                    color = IPulseTheme.colors.textDim
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IPulseTheme.colors.border,
                unfocusedBorderColor = IPulseTheme.colors.borderSoft,
                focusedContainerColor = IPulseTheme.colors.panelInset,
                unfocusedContainerColor = IPulseTheme.colors.panelInset
            ),
            shape = RoundedCornerShape(4.dp),
            textStyle = IPulseTheme.typography.monoMicro.copy(color = IPulseTheme.colors.textPrimary),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (state.filteredDevices.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.filteredDevices, key = { it.id }) { device ->
                    DeviceRow(
                        device = device,
                        onClick = { onAction(DashboardAction.OnDeviceClick(device.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConnectionStatusBar(
    connectionState: ConnectionStateUi,
    signalQuality: SignalQualityUi?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(IPulseTheme.colors.panel, RoundedCornerShape(4.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ConnectionChip(
            state = connectionState.state,
            label = connectionState.displayName
        )

        signalQuality?.let { quality ->
            Text(
                text = quality.displayLabel,
                style = IPulseTheme.typography.monoMicro,
                color = IPulseTheme.colors.textDim
            )
        }
    }
}

@Composable
fun ConnectionChip(
    state: ConnectionState,
    label: String,
    modifier: Modifier = Modifier
) {
    val (color, dimColor) = when (state) {
        ConnectionState.CONNECTED -> IPulseTheme.colors.signalCyan to IPulseTheme.colors.signalCyanDim
        ConnectionState.CONNECTING,
        ConnectionState.RECONNECTING -> IPulseTheme.colors.signalAmber to IPulseTheme.colors.signalAmberDim

        ConnectionState.ERROR -> IPulseTheme.colors.faultRed to IPulseTheme.colors.faultRedDim
        ConnectionState.DISCONNECTED -> IPulseTheme.colors.textDim to IPulseTheme.colors.border
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

@Composable
private fun DeviceRow(
    device: DeviceUi,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(IPulseTheme.colors.panel, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.name,
                style = IPulseTheme.typography.bodySmall,
                color = IPulseTheme.colors.textPrimary
            )
            Text(
                text = device.topic,
                style = IPulseTheme.typography.monoMicro,
                color = IPulseTheme.colors.textDim
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = device.latestValue,
                    style = IPulseTheme.typography.dataSmall,
                    color = if (device.isLive) IPulseTheme.colors.textPrimary else IPulseTheme.colors.textDim
                )
                Text(
                    text = device.unit,
                    style = IPulseTheme.typography.monoMicro,
                    color = IPulseTheme.colors.textDim,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            ConnectionChip(
                state = if (device.connectionState == ConnectionState.CONNECTED)
                    ConnectionState.CONNECTED
                else ConnectionState.DISCONNECTED,
                label = if (device.isLive) "live" else "offline"
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No devices yet",
            style = IPulseTheme.typography.subtitle,
            color = IPulseTheme.colors.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Nothing has published to\nany topic since this app started listening.",
            style = IPulseTheme.typography.caption,
            color = IPulseTheme.colors.textMuted,
            textAlign = TextAlign.Center
        )
    }
}
