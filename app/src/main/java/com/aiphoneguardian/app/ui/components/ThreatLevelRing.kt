package com.aiphoneguardian.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aiphoneguardian.app.domain.model.ThreatLevel
import com.aiphoneguardian.app.ui.theme.*

@Composable
fun ThreatLevelRing(
    threatLevel: ThreatLevel,
    progress: Float = 1f,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ring")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val color = when (threatLevel) {
        ThreatLevel.SAFE -> SafeGreen
        ThreatLevel.WARNING -> WarningYellow
        ThreatLevel.CRITICAL -> CriticalRed
    }

    val glowColor = when (threatLevel) {
        ThreatLevel.SAFE -> SafeGreen.copy(alpha = 0.3f)
        ThreatLevel.WARNING -> WarningYellow.copy(alpha = 0.3f)
        ThreatLevel.CRITICAL -> CriticalRed.copy(alpha = 0.3f)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = size.toPx() * 0.08f
            val arcSize = Size(size.toPx() - strokeWidth, size.toPx() - strokeWidth)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            // Background track
            drawArc(
                color = color.copy(alpha = 0.1f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Glow arc
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        glowColor,
                        color.copy(alpha = 0.6f),
                        glowColor
                    ),
                    center = Offset(size.toPx() / 2, size.toPx() / 2)
                ),
                startAngle = rotation - 90f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth * 1.5f, cap = StrokeCap.Round)
            )

            // Progress arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Text(
            text = when (threatLevel) {
                ThreatLevel.SAFE -> "SAFE"
                ThreatLevel.WARNING -> "WARN"
                ThreatLevel.CRITICAL -> "!"
            },
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun StatusRing(
    label: String,
    value: Float,
    maxValue: Float = 100f,
    color: Color = NeonCyan,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp
) {
    val progress = (value / maxValue).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "ringProgress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = size.toPx() * 0.1f
            val arcSize = Size(size.toPx() - strokeWidth, size.toPx() - strokeWidth)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            drawArc(
                color = color.copy(alpha = 0.15f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Text(
            text = "${value.toInt()}%",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }

    Text(
        text = label,
        fontSize = 12.sp,
        color = TextSecondary
    )
}
