package com.aiphoneguardian.app.ui.screens.scanner

import com.aiphoneguardian.app.ui.screens.auth.GlowButton

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aiphoneguardian.app.domain.model.*
import com.aiphoneguardian.app.ui.components.GlassmorphismCard
import com.aiphoneguardian.app.ui.components.ParticleBackground
import com.aiphoneguardian.app.ui.components.GlowButton
import com.aiphoneguardian.app.ui.theme.*
import com.aiphoneguardian.app.ui.viewmodel.ScannerViewModel

@Composable
fun ScannerScreen(
    onNavigateBack: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val scanState by viewModel.scanState.collectAsState()
    val scanResult by viewModel.scanResult.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()
    val currentScanningItem by viewModel.currentScanningItem.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        ParticleBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = "Security Scanner",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (scanState) {
                is ScannerViewModel.ScanState.Idle -> {
                    ScannerIdleScreen(
                        onQuickScan = { viewModel.startQuickScan() },
                        onFullScan = { viewModel.startFullScan() }
                    )
                }
                is ScannerViewModel.ScanState.Scanning -> {
                    ScanningScreen(
                        progress = scanProgress,
                        currentItem = currentScanningItem,
                        scanType = (scanState as ScannerViewModel.ScanState.Scanning).scanType
                    )
                }
                is ScannerViewModel.ScanState.Complete -> {
                    ScanResultScreen(
                        result = scanResult,
                        onFixAll = { viewModel.fixAllThreats() },
                        onNewScan = { viewModel.resetScan() },
                        onNavigateBack = onNavigateBack
                    )
                }
                is ScannerViewModel.ScanState.Error -> {
                    val errorMsg = (scanState as ScannerViewModel.ScanState.Error).message
                    ScanErrorScreen(
                        message = errorMsg,
                        onRetry = { viewModel.resetScan() }
                    )
                }
            }
        }
    }
}

@Composable
fun ScannerIdleScreen(
    onQuickScan: () -> Unit,
    onFullScan: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Shield icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(60.dp))
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonCyan.copy(alpha = 0.3f), Color.Transparent)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "System Scanner",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Text(
            text = "Scan your device for threats and vulnerabilities",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Quick scan card
        GlassmorphismCard(
            glowColor = NeonCyan,
            onClick = onQuickScan
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(NeonCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Quick Scan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Scans apps & key files (~2 min)",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Full scan card
        GlassmorphismCard(
            glowColor = NeonPurple,
            onClick = onFullScan
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(NeonPurple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = NeonPurple,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Full System Scan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Deep scan all files & network (~10 min)",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextMuted
                )
            }
        }
    }
}

@Composable
fun ScanningScreen(
    progress: Float,
    currentItem: String,
    scanType: ScanType
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanning")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinner"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Animated scanning icon
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                progress = progress / 100f,
                color = NeonCyan,
                strokeWidth = 6.dp,
                trackColor = NeonCyan.copy(alpha = 0.1f)
            )
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = if (scanType == ScanType.QUICK) "Quick Scanning..." else "Full System Scan...",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = currentItem,
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = progress / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = NeonCyan,
            trackColor = GlassWhite.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${progress.toInt()}%",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NeonCyan
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "AI is analyzing your system in real-time...",
            fontSize = 12.sp,
            color = TextMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun ScanResultScreen(
    result: ScanResult?,
    onFixAll: () -> Unit,
    onNewScan: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        result?.let { scanResult ->
            // Result header
            val isSafe = scanResult.overallStatus == ThreatLevel.SAFE
            val headerColor = when (scanResult.overallStatus) {
                ThreatLevel.SAFE -> SafeGreen
                ThreatLevel.WARNING -> WarningYellow
                ThreatLevel.CRITICAL -> CriticalRed
            }

            GlassmorphismCard(glowColor = headerColor) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (isSafe) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = headerColor,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isSafe) "System Clean!" else "${scanResult.threats.size} Threats Found",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = headerColor
                    )

                    if (scanResult.aiReport != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = scanResult.aiReport.summary,
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Scan Duration: ${scanResult.durationMs / 1000}s",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Analysis
            if (scanResult.aiReport?.detailedAnalysis != null) {
                GlassmorphismCard(glowColor = ElectricBlue) {
                    Column {
                        Text(
                            text = "AI Analysis",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = scanResult.aiReport.detailedAnalysis,
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Confidence: ${(scanResult.aiReport.confidenceScore * 100).toInt()}%",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Recommendations
            if (!scanResult.aiReport?.recommendedActions.isNullOrEmpty()) {
                GlassmorphismCard(glowColor = NeonPurple) {
                    Column {
                        Text(
                            text = "Recommended Actions",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonPurple
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        scanResult.aiReport?.recommendedActions?.forEachIndexed { index, action ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${index + 1}.",
                                    fontSize = 14.sp,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = action,
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Threat list
            if (scanResult.threats.isNotEmpty()) {
                Text(
                    text = "Detected Threats",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                scanResult.threats.forEach { threat ->
                    ThreatCard(threat = threat)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                GlowButton(
                    text = "FIX ALL ISSUES",
                    onClick = onFixAll,
                    glowColor = SafeGreen
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedButton(
                onClick = onNewScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "NEW SCAN",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ThreatCard(threat: ThreatItem) {
    val color = when (threat.severity) {
        ThreatLevel.SAFE -> SafeGreen
        ThreatLevel.WARNING -> WarningYellow
        ThreatLevel.CRITICAL -> CriticalRed
    }

    GlassmorphismCard(glowColor = color.copy(alpha = 0.5f)) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (threat.severity) {
                            ThreatLevel.SAFE -> Icons.Default.CheckCircle
                            ThreatLevel.WARNING -> Icons.Default.Warning
                            ThreatLevel.CRITICAL -> Icons.Default.Dangerous
                        },
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = threat.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = threat.type.name.replace("_", " "),
                        fontSize = 12.sp,
                        color = color
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = threat.description,
                fontSize = 13.sp,
                color = TextSecondary
            )

            if (threat.recommendation.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Fix: ${threat.recommendation}",
                    fontSize = 12.sp,
                    color = NeonCyan
                )
            }
        }
    }
}

@Composable
fun ScanErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(64.dp))

        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = CriticalRed,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Scan Error",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = CriticalRed
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        GlowButton(
            text = "RETRY",
            onClick = onRetry
        )
    }
}
