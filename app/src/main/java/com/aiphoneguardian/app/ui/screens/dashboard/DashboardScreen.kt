package com.aiphoneguardian.app.ui.screens.dashboard

import com.aiphoneguardian.app.ui.screens.auth.GlowButton

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aiphoneguardian.app.domain.model.ThreatLevel
import com.aiphoneguardian.app.ui.components.*
import com.aiphoneguardian.app.ui.theme.*
import com.aiphoneguardian.app.ui.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    onNavigateToScanner: () -> Unit,
    onNavigateToPremium: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val systemStatus by viewModel.systemStatus.collectAsState()
    val lastScanResult by viewModel.lastScanResult.collectAsState()
    val isProtectionEnabled by viewModel.isProtectionEnabled.collectAsState()
    val recentAlerts by viewModel.recentAlerts.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()

    Scaffold(
        bottomBar = { /* Bottom nav handled by parent */ }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ParticleBackground(modifier = Modifier.fillMaxSize())

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Header
                DashboardHeader(
                    isPremium = isPremium,
                    onPremiumClick = onNavigateToPremium
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Threat Level Ring
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    ThreatLevelRing(
                        threatLevel = lastScanResult?.overallStatus ?: ThreatLevel.SAFE,
                        progress = if (lastScanResult?.overallStatus == ThreatLevel.SAFE) 1f else 0.75f,
                        size = 180.dp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // AI Guardian Status
                Text(
                    text = "AI Guardian Active",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafeGreen,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Quick Stats Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        StatCard(
                            icon = Icons.Default.Search,
                            label = "Files Scanned",
                            value = (lastScanResult?.filesScanned ?: 0).toString(),
                            color = NeonCyan
                        )
                    }
                    item {
                        StatCard(
                            icon = Icons.Default.Security,
                            label = "Threats Blocked",
                            value = recentAlerts.size.toString(),
                            color = if (recentAlerts.isNotEmpty()) CriticalRed else SafeGreen
                        )
                    }
                    item {
                        StatCard(
                            icon = Icons.Default.Schedule,
                            label = "Last Scan",
                            value = "2h ago",
                            color = ElectricBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // System Monitors
                Text(
                    text = "System Monitor",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    systemStatus?.let { status ->
                        StatusRing(
                            label = "CPU",
                            value = status.cpuUsagePercent,
                            color = NeonCyan
                        )
                        StatusRing(
                            label = "RAM",
                            value = status.ramUsagePercent,
                            color = NeonPurple
                        )
                        StatusRing(
                            label = "Battery",
                            value = status.batteryPercent.toFloat(),
                            color = if (status.batteryPercent > 20) SafeGreen else WarningYellow
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Quick Scan Button
                GlowButton(
                    text = "QUICK SCAN",
                    onClick = onNavigateToScanner,
                    glowColor = NeonCyan
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Full Scan Button
                OutlinedButton(
                    onClick = onNavigateToScanner,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        GlassBorder.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "FULL SYSTEM SCAN",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Real-time Protection Toggle
                GlassmorphismCard(
                    glowColor = if (isProtectionEnabled) SafeGreen else WarningYellow
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (isProtectionEnabled) SafeGreen else WarningYellow,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Real-Time Protection",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (isProtectionEnabled) "Enabled" else "Disabled",
                                    fontSize = 12.sp,
                                    color = if (isProtectionEnabled) SafeGreen else WarningYellow
                                )
                            }
                        }
                        Switch(
                            checked = isProtectionEnabled,
                            onCheckedChange = { viewModel.toggleProtection(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SafeGreen,
                                checkedTrackColor = SafeGreen.copy(alpha = 0.5f),
                                uncheckedThumbColor = WarningYellow,
                                uncheckedTrackColor = WarningYellow.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Recent Alerts
                Text(
                    text = "Recent Alerts",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (recentAlerts.isEmpty()) {
                    GlassmorphismCard {
                        Text(
                            text = "No recent alerts. Your device is secure!",
                            fontSize = 14.sp,
                            color = SafeGreen,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    recentAlerts.take(5).forEach { alert ->
                        AlertCard(alert = alert)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun DashboardHeader(
    isPremium: Boolean,
    onPremiumClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "AI PHONE",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                letterSpacing = 4.sp
            )
            Text(
                text = "GUARDIAN",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = NeonCyan,
                letterSpacing = 6.sp
            )
        }

        if (!isPremium) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(NeonPurple, ElectricBlue)
                        )
                    )
                    .clickable(onClick = onPremiumClick)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.Yellow,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "UPGRADE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SafeGreen.copy(alpha = 0.2f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = SafeGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "PREMIUM",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafeGreen
                )
            }
        }
    }
}

@Composable
fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    GlassmorphismCard(
        modifier = Modifier.width(140.dp),
        glowColor = color
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

data class AlertItem(
    val title: String,
    val description: String,
    val severity: ThreatLevel,
    val timestamp: String
)

@Composable
fun AlertCard(alert: AlertItem) {
    val color = when (alert.severity) {
        ThreatLevel.SAFE -> SafeGreen
        ThreatLevel.WARNING -> WarningYellow
        ThreatLevel.CRITICAL -> CriticalRed
    }

    GlassmorphismCard(glowColor = color.copy(alpha = 0.5f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (alert.severity) {
                        ThreatLevel.SAFE -> Icons.Default.CheckCircle
                        ThreatLevel.WARNING -> Icons.Default.Warning
                        ThreatLevel.CRITICAL -> Icons.Default.Error
                    },
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = alert.description,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 2
                )
            }

            Text(
                text = alert.timestamp,
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}
