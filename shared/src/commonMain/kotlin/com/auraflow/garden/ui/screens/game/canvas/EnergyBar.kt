package com.auraflow.garden.ui.screens.game.canvas

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun EnergyBar(
    energy: Float,
    maxEnergy: Float,
    modifier: Modifier = Modifier,
) {
    val fraction = if (maxEnergy > 0f) (energy / maxEnergy).coerceIn(0f, 1f) else 0f

    val targetBaseColor = when {
        fraction > 0.30f -> Color(0xFF4CAF50) // Green
        fraction > 0.10f -> Color(0xFFFFB300) // Amber
        else -> Color(0xFFF44336)              // Red
    }
    val barColor by animateColorAsState(targetValue = targetBaseColor, label = "energy_color")

    val infiniteTransition = rememberInfiniteTransition(label = "energy_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (fraction <= 0.10f) 0.4f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_alpha",
    )

    val percentText = "${(fraction * 100).toInt()}% energy remaining"
    val effectiveAlpha = if (fraction <= 0.10f) pulseAlpha else 1f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(18.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .semantics { contentDescription = percentText },
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction)
                .clip(RoundedCornerShape(9.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.55f * effectiveAlpha),
                            barColor.copy(alpha = effectiveAlpha),
                            barColor.copy(alpha = 0.75f * effectiveAlpha),
                        ),
                    ),
                ),
        )
    }
}
