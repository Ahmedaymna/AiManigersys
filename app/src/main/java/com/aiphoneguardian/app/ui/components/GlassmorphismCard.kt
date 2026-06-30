package com.aiphoneguardian.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.aiphoneguardian.app.ui.theme.CardShape
import com.aiphoneguardian.app.ui.theme.GlassBorder
import com.aiphoneguardian.app.ui.theme.GlassHighlight
import com.aiphoneguardian.app.ui.theme.GlassWhite
import com.aiphoneguardian.app.ui.theme.NeonCyan

@Composable
fun GlassmorphismCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardShape,
    glowColor: Color = NeonCyan,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = shape,
                spotColor = glowColor.copy(alpha = 0.15f)
            )
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = GlassWhite
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GlassHighlight.copy(alpha = 0.1f),
                            GlassWhite.copy(alpha = 0.05f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            GlassBorder.copy(alpha = 0.4f),
                            glowColor.copy(alpha = 0.2f),
                            GlassBorder.copy(alpha = 0.4f)
                        )
                    ),
                    shape = shape
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

@Composable
fun GlowCard(
    modifier: Modifier = Modifier,
    glowColor: Color = NeonCyan,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = glowColor.copy(alpha = 0.08f),
                shape = CardShape
            )
            .border(
                width = 1.dp,
                color = glowColor.copy(alpha = 0.3f),
                shape = CardShape
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center,
        content = content
    )
}
