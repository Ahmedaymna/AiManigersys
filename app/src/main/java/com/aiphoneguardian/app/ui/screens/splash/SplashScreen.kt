package com.aiphoneguardian.app.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aiphoneguardian.app.ui.theme.*
import com.aiphoneguardian.app.ui.viewmodel.SplashViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToDashboard: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shield")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shieldRotation"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shieldPulse"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val isAuthenticated by viewModel.isAuthenticated.collectAsState()

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated != null) {
            delay(2500)
            if (isAuthenticated == true) {
                onNavigateToDashboard()
            } else {
                onNavigateToLogin()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        SpaceDark,
                        DeepSpaceBlack
                    ),
                    center = Offset(0.5f, 0.4f),
                    radius = 0.8f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Particle background
        ParticleBackgroundSplash()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Shield
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow ring
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    val radius = size.width * 0.45f

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                NeonCyan.copy(alpha = glowAlpha * 0.4f),
                                NeonPurple.copy(alpha = glowAlpha * 0.2f),
                                Color.Transparent
                            ),
                            center = Offset(centerX, centerY),
                            radius = radius * 1.5f
                        ),
                        radius = radius * 1.5f,
                        center = Offset(centerX, centerY)
                    )
                }

                // Shield icon
                Canvas(modifier = Modifier.fillMaxSize(scale)) {
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    val shieldRadius = size.width * 0.35f

                    rotate(rotation, Offset(centerX, centerY)) {
                        drawArc(
                            color = NeonCyan.copy(alpha = 0.3f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 3f, cap = StrokeCap.Round),
                            topLeft = Offset(centerX - shieldRadius * 1.2f, centerY - shieldRadius * 1.2f),
                            size = Size(shieldRadius * 2.4f, shieldRadius * 2.4f)
                        )
                    }

                    // Shield body
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                NeonCyan.copy(alpha = 0.6f),
                                ElectricBlue.copy(alpha = 0.4f)
                            ),
                            center = Offset(centerX, centerY),
                            radius = shieldRadius
                        ),
                        radius = shieldRadius,
                        center = Offset(centerX, centerY)
                    )

                    // Inner shield
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                NeonCyan.copy(alpha = 0.1f)
                            ),
                            center = Offset(centerX, centerY),
                            radius = shieldRadius * 0.6f
                        ),
                        radius = shieldRadius * 0.6f,
                        center = Offset(centerX, centerY)
                    )

                    // Lock icon representation
                    drawCircle(
                        color = Color.White.copy(alpha = 0.9f),
                        radius = shieldRadius * 0.25f,
                        center = Offset(centerX, centerY)
                    )

                    drawCircle(
                        color = NeonCyan,
                        radius = shieldRadius * 0.12f,
                        center = Offset(centerX, centerY)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App name
            Text(
                text = "AI PHONE",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                letterSpacing = 8.sp
            )

            Text(
                text = "GUARDIAN",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = NeonCyan,
                textAlign = TextAlign.Center,
                letterSpacing = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "AI-Powered System Security",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading dots
            LoadingDotsAnimation()
        }
    }
}

@Composable
private fun ParticleBackgroundSplash() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        repeat(15) { i ->
            val x = (i * 73f) % size.width
            val y = (i * 137f) % size.height
            drawCircle(
                color = when (i % 3) {
                    0 -> NeonCyan.copy(alpha = 0.2f)
                    1 -> NeonPurple.copy(alpha = 0.15f)
                    else -> ElectricBlue.copy(alpha = 0.15f)
                },
                radius = (i % 5 + 2) * 2f,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun LoadingDotsAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val dotStates = List(3) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, delayMillis = index * 200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot$index"
        )
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        dotStates.forEach { alpha ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(8.dp)
                    .background(
                        color = NeonCyan.copy(alpha = 0.3f + alpha.value * 0.7f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
        }
    }
}
