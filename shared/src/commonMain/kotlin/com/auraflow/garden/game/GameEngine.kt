package com.auraflow.garden.game

import androidx.compose.ui.geometry.Offset
import com.auraflow.garden.data.model.Node
import com.auraflow.garden.data.model.StarThresholds
import kotlin.math.sqrt

/**
 * Pure game logic — no UI dependencies.
 * All functions are stateless and testable in isolation.
 */
object GameEngine {

    /**
     * Energy cost = distance between node positions * 30f * energyCostMultiplier.
     * Node positions are normalized 0..1, so distance is also 0..1.
     * Max diagonal distance is ~1.41, so max cost per link is ~42 energy.
     * This allows typical 2-pair levels to be completable within 100 energy.
     */
    fun calculateEnergyCost(from: Offset, to: Offset, multiplier: Float): Float {
        val dx = to.x - from.x
        val dy = to.y - from.y
        val distance = sqrt(dx * dx + dy * dy)
        return distance * 30f * multiplier
    }

    /**
     * Score calculation:
     * Base: (energyRemaining/maxEnergy * 1000).toInt()
     * Time bonus: if elapsed < parTime, add ((parTimeMs - elapsedMs) / 1000 * 10).toInt()
     */
    fun calculateScore(
        energyRemaining: Float,
        maxEnergy: Float,
        elapsedMs: Long,
        parTimeMs: Long,
    ): Int {
        val base = (energyRemaining / maxEnergy.coerceAtLeast(1f) * 1000).toInt()
        val timeBonus = if (elapsedMs < parTimeMs) {
            ((parTimeMs - elapsedMs) / 1000L * 10L).toInt()
        } else {
            0
        }
        return base + timeBonus
    }

    /**
     * Stars from energy fraction:
     * 3 stars if fraction >= threeStar, 2 if >= twoStar, 1 if >= oneStar, 0 otherwise
     */
    fun calculateStars(
        energyRemaining: Float,
        maxEnergy: Float,
        thresholds: StarThresholds,
    ): Int {
        val fraction = energyRemaining / maxEnergy.coerceAtLeast(1f)
        return when {
            fraction >= thresholds.threeStar -> 3
            fraction >= thresholds.twoStar -> 2
            fraction >= thresholds.oneStar -> 1
            else -> 0
        }
    }

    /**
     * Standard line segment intersection algorithm.
     * Returns true if segments (p1,p2) and (p3,p4) intersect.
     */
    fun checkIntersection(
        p1: Offset, p2: Offset,
        p3: Offset, p4: Offset,
    ): Boolean {
        val d1 = crossProduct(p3, p4, p1)
        val d2 = crossProduct(p3, p4, p2)
        val d3 = crossProduct(p1, p2, p3)
        val d4 = crossProduct(p1, p2, p4)

        if (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
            ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))
        ) return true

        if (d1 == 0f && onSegment(p3, p4, p1)) return true
        if (d2 == 0f && onSegment(p3, p4, p2)) return true
        if (d3 == 0f && onSegment(p1, p2, p3)) return true
        if (d4 == 0f && onSegment(p1, p2, p4)) return true

        return false
    }

    /**
     * Level is solved when all nodes are linked.
     */
    fun isSolved(nodes: List<Node>): Boolean = nodes.all { it.isLinked }

    /**
     * Crescendo condition: all nodes linked, energy fraction >= 0.65, AND zero intersections.
     * Intersection-free play makes Crescendo harder-earned and more satisfying.
     */
    fun isCrescendo(
        nodes: List<Node>,
        energyRemaining: Float,
        maxEnergy: Float,
        intersectionCount: Int = 0,
    ): Boolean {
        val fraction = energyRemaining / maxEnergy.coerceAtLeast(1f)
        return isSolved(nodes) && fraction >= 0.65f && intersectionCount == 0
    }

    private fun crossProduct(pi: Offset, pj: Offset, pk: Offset): Float {
        return (pk.x - pi.x) * (pj.y - pi.y) - (pj.x - pi.x) * (pk.y - pi.y)
    }

    private fun onSegment(pi: Offset, pj: Offset, pk: Offset): Boolean {
        return minOf(pi.x, pj.x) <= pk.x && pk.x <= maxOf(pi.x, pj.x) &&
            minOf(pi.y, pj.y) <= pk.y && pk.y <= maxOf(pi.y, pj.y)
    }
}
