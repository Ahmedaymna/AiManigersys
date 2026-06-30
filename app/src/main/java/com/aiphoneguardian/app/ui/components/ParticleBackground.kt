package com.aiphoneguardian.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.aiphoneguardian.app.ui.theme.NeonCyan
import com.aiphoneguardian.app.ui.theme.NeonPurple
import com.aiphoneguardian.app.ui.theme.ElectricBlue
import kotlin.random.Random

@Composable
fun ParticleBackground(
    modifier: Modifier = Modifier,
    particleCount: Int = 25
) {
    val particles = remember {
        List(particleCount) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 3f + 1f,
                speedX = (Random.nextFloat() - 0.5f) * 0.0003f,
                speedY = (Random.nextFloat() - 0.5f) * 0.0003f,
                color = listOf(NeonCyan, NeonPurple, ElectricBlue, Color.White).random()
            )
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { _ ->
                particles.forEach { particle ->
                    particle.x += particle.speedX
                    particle.y += particle.speedY

                    if (particle.x < 0) particle.x = 1f
                    if (particle.x > 1) particle.x = 0f
                    if (particle.y < 0) particle.y = 1f
                    if (particle.y > 1) particle.y = 0f
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val px = particle.x * size.width
            val py = particle.y * size.height

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        particle.color.copy(alpha = 0.6f),
                        particle.color.copy(alpha = 0.0f)
                    ),
                    center = Offset(px, py),
                    radius = particle.radius * 6f
                ),
                radius = particle.radius * 6f,
                center = Offset(px, py)
            )

            drawCircle(
                color = particle.color.copy(alpha = 0.8f),
                radius = particle.radius,
                center = Offset(px, py)
            )
        }
    }
}

private data class Particle(
    var x: Float,
    var y: Float,
    val radius: Float,
    val speedX: Float,
    val speedY: Float,
    val color: Color
)
