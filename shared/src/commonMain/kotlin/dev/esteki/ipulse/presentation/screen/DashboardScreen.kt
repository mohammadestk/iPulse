package dev.esteki.ipulse.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.esteki.ipulse.domain.model.DeviceConnectionState
import dev.esteki.ipulse.presentation.model.ConnectionStateUi
import dev.esteki.ipulse.presentation.model.DeviceUi
import dev.esteki.ipulse.presentation.model.SignalQualityUi
import dev.esteki.ipulse.presentation.theme.*

@Composable
fun DashboardRoot(
    onNavigateToDeviceDetail: (String) -> Unit,
    viewModel: dev.esteki.ipulse.presentation.viewmodel.DashboardViewModel
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DashboardEvent.NavigateToDeviceDetail -> onNavigateToDeviceDetail(event.deviceId)
                is DashboardEvent.ShowError -> { /* Handle error */ }
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
            .background(Background)
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Sensors",
            style = TitleStyle,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "broker: broker.emqx.io",
            style = MonoMicroStyle,
            color = TextDim
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Connection status bar
        ConnectionStatusBar(
            connectionState = state.connectionState,
            signalQuality = state.signalQuality
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search bar
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { onAction(DashboardAction.OnSearchQueryChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "filter by topic or name",
                    style = MonoMicroStyle,
                    color = TextDim
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Border,
                unfocusedBorderColor = BorderSoft,
                focusedContainerColor = PanelInset,
                unfocusedContainerColor = PanelInset
            ),
            shape = RoundedCornerShape(4.dp),
            textStyle = MonoMicroStyle.copy(color = TextPrimary),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Device list
        if (state.filteredDevices.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.filteredDevices) { device ->
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
            .background(Panel, RoundedCornerShape(4.dp))
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
                style = MonoMicroStyle,
                color = TextDim
            )
        }
    }
}

@Composable
fun ConnectionChip(
    state: dev.esteki.ipulse.domain.model.ConnectionState,
    label: String,
    modifier: Modifier = Modifier
) {
    val (color, dimColor) = when (state) {
        dev.esteki.ipulse.domain.model.ConnectionState.CONNECTED -> SignalCyan to SignalCyanDim
        dev.esteki.ipulse.domain.model.ConnectionState.CONNECTING,
        dev.esteki.ipulse.domain.model.ConnectionState.RECONNECTING -> SignalAmber to SignalAmberDim
        dev.esteki.ipulse.domain.model.ConnectionState.ERROR -> FaultRed to FaultRedDim
        dev.esteki.ipulse.domain.model.ConnectionState.DISCONNECTED -> TextDim to Border
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
            style = MonoMicroStyle,
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
            .background(Panel, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.name,
                style = BodySmallStyle,
                color = TextPrimary
            )
            Text(
                text = device.topic,
                style = MonoMicroStyle,
                color = TextDim
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = device.latestValue,
                    style = DataSmallStyle,
                    color = if (device.isLive) TextPrimary else TextDim
                )
                Text(
                    text = device.unit,
                    style = MonoMicroStyle,
                    color = TextDim,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            ConnectionChip(
                state = if (device.connectionState == DeviceConnectionState.CONNECTED)
                    dev.esteki.ipulse.domain.model.ConnectionState.CONNECTED
                else dev.esteki.ipulse.domain.model.ConnectionState.DISCONNECTED,
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
            style = SubtitleStyle,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Nothing has published to\nany topic since this app started listening.",
            style = CaptionStyle,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
    }
}
