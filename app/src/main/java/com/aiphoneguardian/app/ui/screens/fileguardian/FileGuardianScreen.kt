package com.aiphoneguardian.app.ui.screens.fileguardian

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aiphoneguardian.app.domain.model.FileAiAnalysis
import com.aiphoneguardian.app.domain.model.FileItem
import com.aiphoneguardian.app.domain.model.FileRiskLevel
import com.aiphoneguardian.app.ui.components.GlassmorphismCard
import com.aiphoneguardian.app.ui.components.GlowButton
import com.aiphoneguardian.app.ui.components.ParticleBackground
import com.aiphoneguardian.app.ui.theme.*
import com.aiphoneguardian.app.ui.viewmodel.FileGuardianViewModel

@Composable
fun FileGuardianScreen(
    viewModel: FileGuardianViewModel = hiltViewModel()
) {
    val files by viewModel.files.collectAsState()
    val selectedFile by viewModel.selectedFile.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val aiAnalysis by viewModel.aiAnalysis.collectAsState()
    val currentPath by viewModel.currentPath.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        ParticleBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "File Guardian",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = currentPath,
                fontSize = 12.sp,
                color = TextMuted,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!isPremium) {
                GlassmorphismCard(
                    glowColor = NeonPurple,
                    onClick = { /* Navigate to premium */ }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = NeonPurple
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Upgrade to Premium for full file access",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.Yellow
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (selectedFile != null) {
                FileAnalysisCard(
                    file = selectedFile!!,
                    analysis = aiAnalysis,
                    isAnalyzing = isAnalyzing,
                    onAnalyze = { viewModel.analyzeFile(selectedFile!!.path) },
                    onBack = { viewModel.clearSelection() },
                    onQuarantine = { viewModel.quarantineFile(selectedFile!!.path) },
                    onMarkTrusted = { viewModel.markTrusted(selectedFile!!.path) }
                )
            } else {
                // File list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(files) { file ->
                        FileItemCard(
                            file = file,
                            onClick = { viewModel.selectFile(file) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FileItemCard(
    file: FileItem,
    onClick: () -> Unit
) {
    val riskColor = file.riskLevel.colorHex().let { hex ->
        try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            TextMuted
        }
    }

    GlassmorphismCard(
        glowColor = riskColor,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                contentDescription = null,
                tint = if (file.isDirectory) WarningYellow else NeonCyan,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1
                )
                Text(
                    text = formatFileSize(file.size),
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            // Risk indicator
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(riskColor.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = file.riskLevel.name,
                    fontSize = 10.sp,
                    color = riskColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FileAnalysisCard(
    file: FileItem,
    analysis: FileAiAnalysis?,
    isAnalyzing: Boolean,
    onAnalyze: () -> Unit,
    onBack: () -> Unit,
    onQuarantine: () -> Unit,
    onMarkTrusted: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
    ) {
        // Back button
        TextButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = NeonCyan
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Back", color = NeonCyan)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // File info
        GlassmorphismCard {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.InsertDriveFile,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = file.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = formatFileSize(file.size),
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isAnalyzing) {
            GlassmorphismCard {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = NeonCyan,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "AI Analyzing file...",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }
        } else if (analysis != null) {
            val riskColor = try {
                Color(android.graphics.Color.parseColor(analysis.riskLevel.colorHex()))
            } catch (e: Exception) {
                TextMuted
            }

            // AI Analysis result
            GlassmorphismCard(glowColor = riskColor) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(riskColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (analysis.riskLevel) {
                                    FileRiskLevel.SAFE, FileRiskLevel.LOW -> Icons.Default.CheckCircle
                                    FileRiskLevel.MEDIUM -> Icons.Default.Warning
                                    FileRiskLevel.HIGH, FileRiskLevel.CRITICAL -> Icons.Default.Dangerous
                                    FileRiskLevel.UNKNOWN -> Icons.Default.Help
                                },
                                contentDescription = null,
                                tint = riskColor
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "AI Risk Assessment",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = analysis.riskLevel.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = riskColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = analysis.analysis,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )

                    if (analysis.behaviors.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Detected behaviors:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        analysis.behaviors.forEach { behavior ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(NeonCyan)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = behavior,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    if (analysis.fileHash != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "SHA-256: ${analysis.fileHash.take(16)}...",
                            fontSize = 11.sp,
                            color = TextMuted,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
        } else {
            // Analyze button
            GlowButton(
                text = "ANALYZE WITH AI",
                onClick = onAnalyze
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        if (!isAnalyzing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onMarkTrusted,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SafeGreen),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SafeGreen.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Trust", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onQuarantine,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CriticalRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CriticalRed.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Quarantine", fontSize = 12.sp)
                }
            }
        }
    }
}

private fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${size / 1024} KB"
        size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
        else -> "${size / (1024 * 1024 * 1024)} GB"
    }
}
