package com.aiphoneguardian.app.ui.screens.networkmonitor

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aiphoneguardian.app.domain.model.ConnectionType
import com.aiphoneguardian.app.domain.model.NetworkConnection
import com.aiphoneguardian.app.domain.model.NetworkStatus
import com.aiphoneguardian.app.domain.model.SystemStatus
import com.aiphoneguardian.app.ui.components.GlassmorphismCard
import com.aiphoneguardian.app.ui.components.ParticleBackground
import com.aiphoneguardian.app.ui.theme.*
import com.aiphoneguardian.app.ui.viewmodel.NetworkMonitorViewModel

@Composable
fun NetworkMonitorScreen(
    viewModel: NetworkMonitorViewModel = hiltViewModel()
) {
    val networkStatus by viewModel.networkStatus.collectAsState()
    val connections by viewModel.connections.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        ParticleBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Text(
                text = "Network Monitor",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Monitor active connections in real-time",
                fontSize = 14.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Network status card
            networkStatus?.let { status ->
                NetworkStatusCard(status = status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Speed monitor
            networkStatus?.let { status ->
                SpeedMonitorCard(
                    uploadSpeed = status.uploadSpeedBps,
                    downloadSpeed = status.downloadSpeedBps
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isPremium) {
                GlassmorphismCard(
                    glowColor = NeonPurple,
                    onClick = { /* Navigate to premium */ }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = NeonPurple
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Upgrade to Premium for full network monitoring",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Active connections
            Text(
                text = "Active Connections (${connections.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (connections.isEmpty()) {
                GlassmorphismCard {
                    Text(
                        text = "No active connections detected",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                connections.take(if (isPremium) 50 else 5).forEach { connection ->
                    ConnectionCard(connection = connection)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun NetworkStatusCard(status: NetworkStatus) {
    val connectionColor = when (status.connectionType) {
        ConnectionType.WIFI -> SafeGreen
        ConnectionType.MOBILE -> NeonCyan
        ConnectionType.VPN -> NeonPurple
        ConnectionType.NONE -> CriticalRed
    }

    GlassmorphismCard(glowColor = connectionColor) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(connectionColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (status.connectionType) {
                                ConnectionType.WIFI -> Icons.Default.Wifi
                                ConnectionType.MOBILE -> Icons.Default.NetworkCell
                                ConnectionType.VPN -> Icons.Default.VpnKey
                                ConnectionType.NONE -> Icons.Default.WifiOff
                            },
                            contentDescription = null,
                            tint = connectionColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = when (status.connectionType) {
                                ConnectionType.WIFI -> "Wi-Fi Connected"
                                ConnectionType.MOBILE -> "Mobile Data"
                                ConnectionType.VPN -> "VPN Active"
                                ConnectionType.NONE -> "No Connection"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (status.isConnected) "Secure Connection" else "No Internet",
                            fontSize = 12.sp,
                            color = if (status.isConnected) SafeGreen else CriticalRed
                        )
                    }
                }

                if (status.suspiciousConnections > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(CriticalRed.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${status.suspiciousConnections} suspicious",
                            fontSize = 12.sp,
                            color = CriticalRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpeedMonitorCard(uploadSpeed: Long, downloadSpeed: Long) {
    GlassmorphismCard {
        Column {
            Text(
                text = "Real-Time Speed",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SpeedIndicator(
                    icon = Icons.Default.ArrowUpward,
                    label = "Upload",
                    speed = uploadSpeed,
                    color = NeonCyan
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(50.dp)
                        .background(GlassBorder.copy(alpha = 0.3f))
                )

                SpeedIndicator(
                    icon = Icons.Default.ArrowDownward,
                    label = "Download",
                    speed = downloadSpeed,
                    color = SafeGreen
                )
            }
        }
    }
}

@Composable
fun SpeedIndicator(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    speed: Long,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speedPulse"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color.copy(alpha = pulse),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formatSpeed(speed),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

@Composable
fun ConnectionCard(connection: NetworkConnection) {
    val isSuspicious = connection.isSuspicious
    val cardColor = if (isSuspicious) CriticalRed else NeonCyan

    GlassmorphismCard(glowColor = cardColor.copy(alpha = if (isSuspicious) 0.6f else 0.3f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(cardColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSuspicious) Icons.Default.Warning else Icons.Default.Router,
                    contentDescription = null,
                    tint = cardColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = connection.remoteAddress,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                Text(
                    text = "Port: ${connection.remotePort} | ${connection.protocol}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                if (connection.appName != null) {
                    Text(
                        text = "App: ${connection.appName}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            if (isSuspicious) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CriticalRed.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "SUSPICIOUS",
                        fontSize = 10.sp,
                        color = CriticalRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatSpeed(bps: Long): String {
    return when {
        bps < 1024 -> "${bps} B/s"
        bps < 1024 * 1024 -> "${bps / 1024} KB/s"
        else -> "${bps / (1024 * 1024)} MB/s"
    }
}
