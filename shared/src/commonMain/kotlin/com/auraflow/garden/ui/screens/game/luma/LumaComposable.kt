package com.auraflow.garden.ui.screens.game.luma

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.auraflow.garden.data.model.LumaEmotion
import com.auraflow.garden.data.model.LumaState

@Composable
fun LumaComposable(
    lumaState: LumaState,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    // Pre-allocate in remember
    val circlePath = remember { Path() }

    val backgroundColor = emotionToColor(lumaState.emotion)
    val emoji = emotionToEmoji(lumaState.emotion)

    AnimatedVisibility(
        visible = lumaState.isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(size)) {
                drawCircle(
                    color = backgroundColor.copy(alpha = 0.9f),
                    radius = this.size.minDimension / 2f,
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.3f),
                    radius = this.size.minDimension / 2f,
                    style = Stroke(width = 2.dp.toPx()),
                )
            }
            Text(
                text = emoji,
                style = if (size >= 48.dp) MaterialTheme.typography.headlineSmall
                        else MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun emotionToColor(emotion: LumaEmotion): Color = when (emotion) {
    LumaEmotion.IDLE -> Color(0xFF8B5CF6)        // Violet
    LumaEmotion.HAPPY -> Color(0xFF00B4D8)        // Teal
    LumaEmotion.EXCITED -> Color(0xFFFFB703)      // Amber
    LumaEmotion.WORRIED -> Color(0xFFFF6B6B)      // Coral
    LumaEmotion.SAD -> Color(0xFF4338CA)           // Indigo
    LumaEmotion.THINKING -> Color(0xFF06D6A0)     // Emerald
    LumaEmotion.CELEBRATING -> Color(0xFFE63946)  // Rose
}

private fun emotionToEmoji(emotion: LumaEmotion): String = when (emotion) {
    LumaEmotion.IDLE -> "✨"
    LumaEmotion.HAPPY -> "😊"
    LumaEmotion.EXCITED -> "🌟"
    LumaEmotion.WORRIED -> "😟"
    LumaEmotion.SAD -> "😢"
    LumaEmotion.THINKING -> "🤔"
    LumaEmotion.CELEBRATING -> "🎉"
}
