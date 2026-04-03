package com.auraflow.garden.ui.effects

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.auraflow.garden.data.model.GameStatus
import com.auraflow.garden.data.model.Node
import com.auraflow.garden.ui.screens.game.canvas.parseHexColor
import kotlin.random.Random
import kotlin.time.Clock

@Composable
fun EffectsLayer(
    nodes: List<Node>,
    gameStatus: GameStatus,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    crescendoTriggered: Boolean,
    fizzleAt: Offset?,
    modifier: Modifier = Modifier,
) {
    val particleSystem = remember { ParticleSystem() }
    var particles by remember { mutableStateOf(particleSystem.getAlive()) }
    var lastAmbientMs by remember { mutableStateOf(0L) }
    var celebrationFired by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "crescendo")
    val crescendoScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (crescendoTriggered) 1.35f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "crescendo_scale",
    )

    // Crescendo burst
    LaunchedEffect(crescendoTriggered) {
        if (crescendoTriggered && canvasWidthPx > 0f && canvasHeightPx > 0f) {
            for (node in nodes) {
                val nx = node.position.x * canvasWidthPx
                val ny = node.position.y * canvasHeightPx
                particleSystem.emitCrescendo(nx, ny, parseHexColor(node.colorType.hex))
            }
        }
    }

    // Fizzle (invalid move)
    LaunchedEffect(fizzleAt) {
        fizzleAt?.let { pos ->
            if (canvasWidthPx > 0f) particleSystem.emitFizzle(pos.x, pos.y)
        }
    }

    // Win celebration — fires once when status transitions to WON
    LaunchedEffect(gameStatus) {
        if (gameStatus == GameStatus.WON && canvasWidthPx > 0f && !celebrationFired) {
            celebrationFired = true
            // Burst from all nodes
            for (node in nodes) {
                val nx = node.position.x * canvasWidthPx
                val ny = node.position.y * canvasHeightPx
                particleSystem.emitPairComplete(nx, ny, parseHexColor(node.colorType.hex))
            }
            // Then confetti from above
            particleSystem.emitCelebration(canvasWidthPx, canvasHeightPx)
        }
        if (gameStatus == GameStatus.PLAYING) celebrationFired = false
    }

    // Animation loop
    LaunchedEffect(Unit) {
        var lastFrameNanos = Clock.System.now().toEpochMilliseconds() * 1_000_000L
        while (true) {
            withFrameNanos { frameNanos ->
                val deltaNanos = frameNanos - lastFrameNanos
                lastFrameNanos = frameNanos
                val deltaMs = (deltaNanos / 1_000_000L).coerceIn(0L, 100L)
                val nowMs = frameNanos / 1_000_000L

                // Emit ambient sparkles periodically
                if (canvasWidthPx > 0f && nowMs - lastAmbientMs > 400L) {
                    lastAmbientMs = nowMs
                    val randomX = Random.nextFloat() * canvasWidthPx
                    val randomY = canvasHeightPx * 0.3f + Random.nextFloat() * canvasHeightPx * 0.5f
                    particleSystem.emitAmbientSparkle(randomX, randomY)
                }

                particleSystem.update(deltaMs)
                particles = particleSystem.getAlive()
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        // Node bloom
        for (node in nodes) {
            if (canvasWidthPx <= 0f || canvasHeightPx <= 0f) continue
            val nx = node.position.x * canvasWidthPx
            val ny = node.position.y * canvasHeightPx
            val nodeColor = parseHexColor(node.colorType.hex)
            val scale = if (crescendoTriggered && node.isLinked) crescendoScale else 1f
            drawBloom(Offset(nx, ny), nodeColor, scale)
        }

        // Particles
        for (particle in particles) {
            val pCenter = Offset(particle.x, particle.y)
            val alpha = particle.currentAlpha
            val pScale = particle.currentScale

            if (particle.isConfetti) {
                drawConfettiPiece(particle, pCenter, pScale, alpha)
            } else {
                // Glow halo
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(particle.color.copy(alpha = alpha * 0.5f), Color.Transparent),
                        center = pCenter,
                        radius = 14f * pScale,
                    ),
                    radius = 14f * pScale,
                    center = pCenter,
                )
                // Core
                drawCircle(color = particle.color.copy(alpha = alpha), radius = 5f * pScale, center = pCenter)
                drawCircle(color = Color.White.copy(alpha = alpha * 0.7f), radius = 2.5f * pScale, center = pCenter)
            }
        }
    }
}

private fun DrawScope.drawConfettiPiece(
    particle: Particle,
    center: Offset,
    scale: Float,
    alpha: Float,
) {
    val halfSize = 8f * scale
    rotate(degrees = particle.rotationDeg, pivot = center) {
        drawRect(
            color = particle.color.copy(alpha = alpha),
            topLeft = Offset(center.x - halfSize, center.y - halfSize * 0.5f),
            size = Size(halfSize * 2f, halfSize),
        )
        // Highlight strip
        drawRect(
            color = Color.White.copy(alpha = alpha * 0.3f),
            topLeft = Offset(center.x - halfSize, center.y - halfSize * 0.5f),
            size = Size(halfSize * 2f, halfSize * 0.25f),
        )
    }
}

private fun DrawScope.drawBloom(center: Offset, color: Color, scale: Float) {
    val r = 30f * scale
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.18f), Color.Transparent),
            center = center, radius = r * 3.2f,
        ),
        radius = r * 3.2f, center = center,
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.35f), Color.Transparent),
            center = center, radius = r * 1.9f,
        ),
        radius = r * 1.9f, center = center,
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.55f), Color.Transparent),
            center = center, radius = r * 1.2f,
        ),
        radius = r * 1.2f, center = center,
    )
}
