package com.aiphoneguardian.app.ui.screens.premium

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aiphoneguardian.app.ui.components.GlassmorphismCard
import com.aiphoneguardian.app.ui.components.GlowButton
import com.aiphoneguardian.app.ui.components.ParticleBackground
import com.aiphoneguardian.app.ui.theme.*
import com.aiphoneguardian.app.ui.viewmodel.PremiumViewModel

@Composable
fun PremiumScreen(
    onNavigateBack: () -> Unit,
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isPremium by viewModel.isPremium.collectAsState()
    val activationState by viewModel.activationState.collectAsState()
    var activationCode by remember { mutableStateOf("") }

    LaunchedEffect(activationState) {
        if (activationState is PremiumViewModel.ActivationState.Success) {
            // Show success, maybe navigate back after delay
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ParticleBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
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
                    text = "Premium",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            if (isPremium) {
                PremiumActiveCard(onNavigateBack = onNavigateBack)
            } else {
                Spacer(modifier = Modifier.height(16.dp))

                // Premium header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    NeonPurple.copy(alpha = 0.3f),
                                    ElectricBlue.copy(alpha = 0.3f)
                                )
                            )
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.Yellow,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Unlock Full Protection",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Get unlimited AI-powered security",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Features comparison
                Text(
                    text = "Features Comparison",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                )

                FeatureComparisonTable()

                Spacer(modifier = Modifier.height(24.dp))

                // Activation code section
                GlassmorphismCard(glowColor = NeonPurple) {
                    Column {
                        Text(
                            text = "Have an activation code?",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Enter your code below to activate Premium",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = activationCode,
                            onValueChange = { activationCode = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter activation code", color = TextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = GlassWhite.copy(alpha = 0.1f),
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = NeonPurple.copy(alpha = 0.6f),
                                unfocusedBorderColor = GlassBorder.copy(alpha = 0.3f),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (activationState is PremiumViewModel.ActivationState.Error) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = (activationState as PremiumViewModel.ActivationState.Error).message,
                                fontSize = 13.sp,
                                color = CriticalRed
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        GlowButton(
                            text = "ACTIVATE",
                            onClick = {
                                if (activationCode.isNotBlank()) {
                                    viewModel.activatePremium(activationCode)
                                }
                            },
                            isLoading = activationState is PremiumViewModel.ActivationState.Loading,
                            glowColor = NeonPurple
                        )

                        if (activationState is PremiumViewModel.ActivationState.Success) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Premium activated successfully!",
                                fontSize = 14.sp,
                                color = SafeGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // WhatsApp upgrade button
                Text(
                    text = "Or contact us via WhatsApp to purchase Premium",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                GlowButton(
                    text = "UPGRADE VIA WHATSAPP",
                    onClick = {
                        val phoneNumber = "+201121669958"
                        val message = "\u0645\u0631\u062D\u0628\u0627\u060C \u0623\u0631\u064A\u062F \u062A\u0641\u0639\u064A\u0644 \u062D\u0633\u0627\u0628 Premium \u0641\u064A \u062A\u0637\u0628\u064A\u0642 AI Phone Guardian"
                        val url = "https://wa.me/$phoneNumber?text=${Uri.encode(message)}"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                    },
                    glowColor = SafeGreen
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun PremiumActiveCard(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(100.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(60.dp))
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(SafeGreen.copy(alpha = 0.3f), Color.Transparent)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = SafeGreen,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Premium Active",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = SafeGreen
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You have full access to all AI Guardian features",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Active features
        val features = listOf(
            "Unlimited System Scans" to true,
            "Unlimited AI Chat" to true,
            "File Guardian Access" to true,
            "Network Monitor" to true,
            "PDF Report Export" to true,
            "No Advertisements" to true,
            "Priority Threat Detection" to true
        )

        GlassmorphismCard {
            Column {
                features.forEach { (feature, _) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SafeGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = feature,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = onNavigateBack,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder.copy(alpha = 0.5f))
        ) {
            Text("Back to Dashboard")
        }
    }
}

@Composable
fun FeatureComparisonTable() {
    val features = listOf(
        Triple("Unlimited System Scans", true, false),
        Triple("Unlimited AI Chat", true, false),
        Triple("File Guardian Access", true, false),
        Triple("Network Monitor", true, false),
        Triple("PDF Report Export", true, false),
        Triple("No Advertisements", true, false),
        Triple("Priority Threat Detection", true, false),
        Triple("Basic Scan (Daily)", true, true),
        Triple("Limited AI Chat (5/day)", true, true),
        Triple("Basic Notifications", true, true)
    )

    GlassmorphismCard {
        Column {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Feature",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    modifier = Modifier.weight(1.5f)
                )
                Text(
                    text = "Free",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(0.5f)
                )
                Text(
                    text = "Premium",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(0.5f)
                )
            }

            Divider(
                color = GlassBorder.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Feature rows
            features.forEach { (feature, free, premium) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = feature,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.weight(1.5f)
                    )

                    Box(
                        modifier = Modifier.weight(0.5f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (free) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = SafeGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = CriticalRed.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier.weight(0.5f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (premium) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = CriticalRed.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
