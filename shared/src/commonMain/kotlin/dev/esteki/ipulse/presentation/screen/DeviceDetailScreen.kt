package dev.esteki.ipulse.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.esteki.ipulse.domain.model.ConnectionState
import dev.esteki.ipulse.presentation.model.ConnectionEventUi
import dev.esteki.ipulse.presentation.model.SignalQualityUi
import dev.esteki.ipulse.presentation.theme.IPulseTheme
import dev.esteki.ipulse.presentation.viewmodel.DeviceDetailViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DeviceDetailRoot(
    onNavigateBack: () -> Unit,
    viewModel: DeviceDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DeviceDetailEvent.NavigateBack -> onNavigateBack()
                is DeviceDetailEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    DeviceDetailScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction
    )
}

@Composable
fun DeviceDetailScreen(
    state: DeviceDetailState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAction: (DeviceDetailAction) -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = IPulseTheme.colors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { onAction(DeviceDetailAction.OnBackClick) }) {
                    Text(
                        text = "< Back",
                        style = IPulseTheme.typography.buttonLabel,
                        color = IPulseTheme.colors.textMuted
                    )
                }
                TextButton(onClick = { onAction(DeviceDetailAction.OnRefreshClick) }) {
                    Text(
                        text = "Refresh",
                        style = IPulseTheme.typography.buttonLabel,
                        color = IPulseTheme.colors.textMuted
                    )
                }
            }

            Text(
                text = state.device?.name ?: "Unknown Device",
                style = IPulseTheme.typography.title,
                color = IPulseTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = state.device?.topic ?: "",
                style = IPulseTheme.typography.monoMicro,
                color = IPulseTheme.colors.textDim
            )

            Spacer(modifier = Modifier.height(16.dp))

            ReadoutPanel(
                latestValue = state.latestValue,
                unit = state.unit,
                sensorType = state.sensorType,
                connectionState = state.connectionState,
                signalQuality = state.signalQuality
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Connection log",
                style = IPulseTheme.typography.dataSmall,
                color = IPulseTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (state.connectionEvents.isEmpty()) {
                Text(
                    text = "No connection events yet",
                    style = IPulseTheme.typography.caption,
                    color = IPulseTheme.colors.textDim
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(state.connectionEvents) { event ->
                        ConnectionEventRow(event = event)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadoutPanel(
    latestValue: String,
    unit: String,
    sensorType: String,
    connectionState: ConnectionState,
    signalQuality: SignalQualityUi?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(IPulseTheme.colors.panel, RoundedCornerShape(4.dp))
            .padding(22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = sensorType,
                style = IPulseTheme.typography.overline,
                color = IPulseTheme.colors.textDim
            )
            ConnectionChip(
                state = connectionState,
                label = when (connectionState) {
                    is ConnectionState.Connected -> "Live"
                    is ConnectionState.Reconnecting -> "Reconnecting"
                    is ConnectionState.Connecting -> "Connecting"
                    is ConnectionState.Error -> "Error"
                    is ConnectionState.Disconnected -> "Offline"
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = latestValue,
                style = IPulseTheme.typography.dataLarge,
                color = if (connectionState is ConnectionState.Connected) IPulseTheme.colors.signalCyan else IPulseTheme.colors.textDim
            )
            Text(
                text = unit,
                style = IPulseTheme.typography.dataMedium,
                color = IPulseTheme.colors.textDim,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
        }

        if (signalQuality != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = signalQuality.displayLabel,
                style = IPulseTheme.typography.monoMicro,
                color = IPulseTheme.colors.textDim
            )
        }
    }
}

@Composable
private fun ConnectionEventRow(event: ConnectionEventUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = event.displayTime,
            style = IPulseTheme.typography.monoMicro,
            color = IPulseTheme.colors.textDim
        )
        Text(
            text = event.message,
            style = IPulseTheme.typography.caption,
            color = IPulseTheme.colors.textMuted
        )
    }
    HorizontalDivider(color = IPulseTheme.colors.borderSoft)
}
