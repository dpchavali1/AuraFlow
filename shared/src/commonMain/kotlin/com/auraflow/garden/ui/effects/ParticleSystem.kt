package com.auraflow.garden.ui.effects

import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class ParticleSystem {

    private val _particles = mutableListOf<Particle>()

    val particles: List<Particle> get() = _particles.filter { it.isAlive }

    fun emit(
        x: Float,
        y: Float,
        color: Color,
        count: Int = 12,
        speedRange: ClosedFloatingPointRange<Float> = 0.5f..2f,
        lifetimeMs: Long = 600L,
    ) {
        val angleStep = (2f * PI.toFloat()) / count
        repeat(count) { i ->
            val angle = angleStep * i + Random.nextFloat() * 0.5f
            val speed = speedRange.start + Random.nextFloat() * (speedRange.endInclusive - speedRange.start)
            _particles.add(
                Particle(
                    id = "p_${_particles.size}_${i}",
                    x = x, y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    alpha = 0.9f,
                    scale = Random.nextFloat() * 0.5f + 0.5f,
                    color = color,
                    lifetimeMs = lifetimeMs + Random.nextLong(200L),
                )
            )
        }
    }

    fun emitFizzle(x: Float, y: Float) {
        emit(x = x, y = y, color = Color(0xFFE63946), count = 8, speedRange = 1f..3f, lifetimeMs = 200L)
    }

    fun emitCrescendo(x: Float, y: Float, color: Color) {
        emit(x = x, y = y, color = color, count = 20, speedRange = 1f..4f, lifetimeMs = 1000L)
    }

    fun emitIntersectionBurst(x: Float, y: Float) {
        emit(x = x, y = y, color = Color(0xFFFF6B35), count = 10, speedRange = 0.5f..2f, lifetimeMs = 300L)
    }

    /** Pair-complete ring burst — satisfying flash when two nodes connect. */
    fun emitPairComplete(x: Float, y: Float, color: Color) {
        val count = 16
        val angleStep = (2f * PI.toFloat()) / count
        repeat(count) { i ->
            val angle = angleStep * i
            val speed = 2.5f + Random.nextFloat() * 1.5f
            _particles.add(
                Particle(
                    id = "pair_${_particles.size}_$i",
                    x = x, y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    alpha = 1.0f,
                    scale = 0.6f + Random.nextFloat() * 0.4f,
                    color = color,
                    lifetimeMs = 500L + Random.nextLong(200L),
                )
            )
        }
        // Central bright flash
        repeat(6) { i ->
            _particles.add(
                Particle(
                    id = "flash_${_particles.size}_$i",
                    x = x + Random.nextFloat() * 8f - 4f,
                    y = y + Random.nextFloat() * 8f - 4f,
                    vx = (Random.nextFloat() - 0.5f) * 0.5f,
                    vy = (Random.nextFloat() - 0.5f) * 0.5f,
                    alpha = 1.0f,
                    scale = 1.2f + Random.nextFloat() * 0.6f,
                    color = Color.White,
                    lifetimeMs = 280L,
                )
            )
        }
    }

    /** Full-screen confetti celebration on level win. */
    fun emitCelebration(canvasWidth: Float, canvasHeight: Float) {
        val confettiColors = listOf(
            Color(0xFF8B5CF6), Color(0xFF00B4D8), Color(0xFFE63946),
            Color(0xFFFFB703), Color(0xFFFF6B6B), Color(0xFF06D6A0),
            Color(0xFFF0F0F5), Color(0xFF4338CA),
        )
        repeat(65) { i ->
            val startX = Random.nextFloat() * canvasWidth
            val startY = -20f - Random.nextFloat() * 300f  // above screen, spread higher
            val speedX = (Random.nextFloat() - 0.5f) * 2f
            val speedY = 1.2f + Random.nextFloat() * 2.5f  // falls slower
            val color = confettiColors[i % confettiColors.size]
            _particles.add(
                Particle(
                    id = "conf_${_particles.size}_$i",
                    x = startX, y = startY,
                    vx = speedX,
                    vy = speedY,
                    alpha = 0.95f,
                    scale = 0.9f + Random.nextFloat() * 0.8f,
                    color = color,
                    lifetimeMs = 4500L + Random.nextLong(1500L),
                    gravity = 0.035f,
                    isConfetti = true,
                    rotationDeg = Random.nextFloat() * 360f,
                    rotationSpeedDps = (Random.nextFloat() - 0.5f) * 5f,
                )
            )
        }
    }

    /** Ambient drifting background sparkles — keeps the canvas feeling alive. */
    fun emitAmbientSparkle(x: Float, y: Float) {
        val colors = listOf(
            Color(0xFF8B5CF6), Color(0xFF00B4D8), Color(0xFFFFB703), Color(0xFF06D6A0),
        )
        _particles.add(
            Particle(
                id = "amb_${_particles.size}",
                x = x, y = y,
                vx = (Random.nextFloat() - 0.5f) * 0.3f,
                vy = -0.4f - Random.nextFloat() * 0.6f,  // drifts upward
                alpha = 0.5f + Random.nextFloat() * 0.3f,
                scale = 0.2f + Random.nextFloat() * 0.3f,
                color = colors.random(),
                lifetimeMs = 3000L + Random.nextLong(2000L),
            )
        )
    }

    fun update(deltaMs: Long) {
        val iterator = _particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            val newVy = p.vy + p.gravity * deltaMs
            val updated = p.copy(
                x = p.x + p.vx * (deltaMs / 16f),
                y = p.y + newVy * (deltaMs / 16f),
                vy = newVy,
                rotationDeg = p.rotationDeg + p.rotationSpeedDps * (deltaMs / 16f),
                elapsedMs = p.elapsedMs + deltaMs,
            )
            if (!updated.isAlive) {
                iterator.remove()
            } else {
                val idx = _particles.indexOf(p)
                if (idx >= 0) _particles[idx] = updated
            }
        }
    }

    fun getAlive(): List<Particle> = _particles.filter { it.isAlive }

    fun clear() { _particles.clear() }
}
