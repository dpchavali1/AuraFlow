package com.auraflow.garden.ui.effects

import androidx.compose.ui.graphics.Color

data class Particle(
    val id: String,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val alpha: Float,
    val scale: Float,
    val color: Color,
    val lifetimeMs: Long,
    val elapsedMs: Long = 0,
    val gravity: Float = 0f,       // px/ms² downward acceleration (positive = falls)
    val isConfetti: Boolean = false, // render as rotating square instead of circle
    val rotationDeg: Float = 0f,    // current rotation angle for confetti
    val rotationSpeedDps: Float = 0f, // degrees per ms
) {
    val isAlive: Boolean get() = elapsedMs < lifetimeMs
    val progress: Float get() = (elapsedMs.toFloat() / lifetimeMs.toFloat()).coerceIn(0f, 1f)
    val currentAlpha: Float get() = alpha * (1f - progress)
    val currentScale: Float get() = if (isConfetti) scale else scale * (1f - progress * 0.5f)
}
