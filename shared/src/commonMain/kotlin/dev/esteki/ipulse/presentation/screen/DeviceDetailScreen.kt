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
import dev.esteki.ipulse.presentation.theme.*
import dev.esteki.ipulse.presentation.viewmodel.DeviceDetailViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun DeviceDetailRoot(
    onNavigateBack: () -> Unit,
    viewModel: DeviceDetailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DeviceDetailEvent.NavigateBack -> onNavigateBack()
                is DeviceDetailEvent.ShowError -> { /* Handle error */ }
            }
        }
    }

    DeviceDetailScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun DeviceDetailScreen(
    state: DeviceDetailState,
    onAction: (DeviceDetailAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { onAction(DeviceDetailAction.OnBackClick) }) {
                Text(
                    text = "< Back",
                    style = ButtonLabelStyle,
                    color = TextMuted
                )
            }
            TextButton(onClick = { onAction(DeviceDetailAction.OnRefreshClick) }) {
                Text(
                    text = "Refresh",
                    style = ButtonLabelStyle,
                    color = TextMuted
                )
            }
        }

        // Device name
        Text(
            text = state.device?.name ?: "Unknown Device",
            style = TitleStyle,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = state.device?.topic ?: "",
            style = MonoMicroStyle,
            color = TextDim
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Main readout panel
        ReadoutPanel(
            latestValue = state.latestValue,
            unit = state.unit,
            sensorType = state.sensorType,
            connectionState = state.connectionState,
            signalQuality = state.signalQuality
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Connection log header
        Text(
            text = "Connection log",
            style = DataSmallStyle,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Connection events
        if (state.connectionEvents.isEmpty()) {
            Text(
                text = "No connection events yet",
                style = CaptionStyle,
                color = TextDim
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

@Composable
private fun ReadoutPanel(
    latestValue: String,
    unit: String,
    sensorType: String,
    connectionState: ConnectionState,
    signalQuality: SignalQualityUi?
) {
    val borderColor = when (connectionState) {
        ConnectionState.CONNECTED -> SignalCyanDim
        ConnectionState.RECONNECTING,
        ConnectionState.CONNECTING -> SignalAmberDim
        ConnectionState.ERROR -> FaultRedDim
        ConnectionState.DISCONNECTED -> Border
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Panel, RoundedCornerShape(4.dp))
            .padding(22.dp)
    ) {
        // Label row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = sensorType,
                style = OverlineStyle,
                color = TextDim
            )
            val chipState = when (connectionState) {
                ConnectionState.CONNECTED -> ConnectionState.CONNECTED
                ConnectionState.CONNECTING,
                ConnectionState.RECONNECTING -> ConnectionState.RECONNECTING
                ConnectionState.ERROR -> ConnectionState.ERROR
                ConnectionState.DISCONNECTED -> ConnectionState.DISCONNECTED
            }
            ConnectionChip(
                state = chipState,
                label = when (connectionState) {
                    ConnectionState.CONNECTED -> "Live"
                    ConnectionState.RECONNECTING -> "Reconnecting"
                    ConnectionState.CONNECTING -> "Connecting"
                    ConnectionState.ERROR -> "Error"
                    ConnectionState.DISCONNECTED -> "Offline"
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Value
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = latestValue,
                style = DataLargeStyle,
                color = if (connectionState == ConnectionState.CONNECTED) SignalCyan else TextDim
            )
            Text(
                text = unit,
                style = DataMediumStyle,
                color = TextDim,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
        }

        // Signal quality
        if (signalQuality != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = signalQuality.displayLabel,
                style = MonoMicroStyle,
                color = TextDim
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
            style = MonoMicroStyle,
            color = TextDim
        )
        Text(
            text = event.message,
            style = CaptionStyle,
            color = TextMuted
        )
    }
    HorizontalDivider(color = BorderSoft)
}
