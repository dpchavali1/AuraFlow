package com.auraflow.garden.game

import androidx.compose.ui.geometry.Offset
import com.auraflow.garden.data.model.Node
import com.auraflow.garden.data.model.NodeColor
import com.auraflow.garden.data.model.StarThresholds
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameEngineTest {

    // ---- Energy Cost Tests ----

    @Test
    fun `calculateEnergyCost - horizontal distance 1 with multiplier 1`() {
        val from = Offset(0f, 0f)
        val to = Offset(1f, 0f)
        val cost = GameEngine.calculateEnergyCost(from, to, 1.0f)
        assertEquals(30f, cost, absoluteTolerance = 0.001f)
    }

    @Test
    fun `calculateEnergyCost - diagonal distance with multiplier`() {
        val from = Offset(0f, 0f)
        val to = Offset(0.3f, 0.4f)
        val expected = sqrt(0.3f * 0.3f + 0.4f * 0.4f) * 30f * 1.2f
        val cost = GameEngine.calculateEnergyCost(from, to, 1.2f)
        assertEquals(expected, cost, absoluteTolerance = 0.001f)
    }

    @Test
    fun `calculateEnergyCost - zero distance`() {
        val pos = Offset(0.5f, 0.5f)
        val cost = GameEngine.calculateEnergyCost(pos, pos, 1.0f)
        assertEquals(0f, cost, absoluteTolerance = 0.001f)
    }

    @Test
    fun `calculateEnergyCost - scales with multiplier`() {
        val from = Offset(0f, 0f)
        val to = Offset(1f, 0f)
        val cost1x = GameEngine.calculateEnergyCost(from, to, 1.0f)
        val cost2x = GameEngine.calculateEnergyCost(from, to, 2.0f)
        assertEquals(cost1x * 2f, cost2x, absoluteTolerance = 0.001f)
    }

    // ---- Score Tests ----

    @Test
    fun `calculateScore - full energy no time bonus`() {
        val score = GameEngine.calculateScore(
            energyRemaining = 100f,
            maxEnergy = 100f,
            elapsedMs = 90_000L,  // 90s > 60s par time
            parTimeMs = 60_000L,
        )
        assertEquals(1000, score)
    }

    @Test
    fun `calculateScore - half energy with time bonus`() {
        val score = GameEngine.calculateScore(
            energyRemaining = 50f,
            maxEnergy = 100f,
            elapsedMs = 30_000L,  // 30s elapsed, 60s par → 30s bonus
            parTimeMs = 60_000L,
        )
        val expectedBase = 500
        val expectedBonus = ((60_000L - 30_000L) / 1000L * 10L).toInt() // 300
        assertEquals(expectedBase + expectedBonus, score)
    }

    @Test
    fun `calculateScore - at par time no bonus`() {
        val score = GameEngine.calculateScore(
            energyRemaining = 100f,
            maxEnergy = 100f,
            elapsedMs = 60_000L,
            parTimeMs = 60_000L,
        )
        assertEquals(1000, score)
    }

    @Test
    fun `calculateScore - zero energy gives zero base`() {
        val score = GameEngine.calculateScore(
            energyRemaining = 0f,
            maxEnergy = 100f,
            elapsedMs = 90_000L,
            parTimeMs = 60_000L,
        )
        assertEquals(0, score)
    }

    // ---- Stars Tests ----

    @Test
    fun `calculateStars - three stars at threshold`() {
        val thresholds = StarThresholds(oneStar = 0.3f, twoStar = 0.5f, threeStar = 0.8f)
        val stars = GameEngine.calculateStars(80f, 100f, thresholds)
        assertEquals(3, stars)
    }

    @Test
    fun `calculateStars - two stars`() {
        val thresholds = StarThresholds(oneStar = 0.3f, twoStar = 0.5f, threeStar = 0.8f)
        val stars = GameEngine.calculateStars(60f, 100f, thresholds)
        assertEquals(2, stars)
    }

    @Test
    fun `calculateStars - one star`() {
        val thresholds = StarThresholds(oneStar = 0.3f, twoStar = 0.5f, threeStar = 0.8f)
        val stars = GameEngine.calculateStars(40f, 100f, thresholds)
        assertEquals(1, stars)
    }

    @Test
    fun `calculateStars - zero stars`() {
        val thresholds = StarThresholds(oneStar = 0.3f, twoStar = 0.5f, threeStar = 0.8f)
        val stars = GameEngine.calculateStars(20f, 100f, thresholds)
        assertEquals(0, stars)
    }

    @Test
    fun `calculateStars - exactly at three star threshold`() {
        val thresholds = StarThresholds(oneStar = 0.3f, twoStar = 0.5f, threeStar = 0.8f)
        val stars = GameEngine.calculateStars(80f, 100f, thresholds)
        assertEquals(3, stars)
    }

    // ---- Intersection Tests ----

    @Test
    fun `checkIntersection - crossing segments`() {
        val result = GameEngine.checkIntersection(
            Offset(0f, 0f), Offset(1f, 1f),  // diagonal top-left to bottom-right
            Offset(0f, 1f), Offset(1f, 0f),  // diagonal bottom-left to top-right
        )
        assertTrue(result)
    }

    @Test
    fun `checkIntersection - parallel non-intersecting segments`() {
        val result = GameEngine.checkIntersection(
            Offset(0f, 0f), Offset(1f, 0f),  // horizontal at y=0
            Offset(0f, 1f), Offset(1f, 1f),  // horizontal at y=1
        )
        assertFalse(result)
    }

    @Test
    fun `checkIntersection - T-intersection segments do not cross`() {
        // Segments that share an endpoint should not be considered intersecting
        val result = GameEngine.checkIntersection(
            Offset(0f, 0f), Offset(1f, 0f),
            Offset(0.5f, 0f), Offset(0.5f, 1f),  // T-shape - shares midpoint of first
        )
        // The midpoint is on the first segment — collinear endpoint check
        assertTrue(result) // They do technically intersect at (0.5, 0)
    }

    @Test
    fun `checkIntersection - non-overlapping collinear segments`() {
        val result = GameEngine.checkIntersection(
            Offset(0f, 0f), Offset(0.3f, 0f),  // left segment
            Offset(0.7f, 0f), Offset(1f, 0f),  // right segment
        )
        assertFalse(result)
    }

    @Test
    fun `checkIntersection - segments at right angles do not cross if not overlapping`() {
        val result = GameEngine.checkIntersection(
            Offset(0f, 0f), Offset(0.4f, 0f),
            Offset(0.5f, -0.5f), Offset(0.5f, -0.1f),
        )
        assertFalse(result)
    }

    // ---- isSolved Tests ----

    @Test
    fun `isSolved - all nodes linked returns true`() {
        val nodes = listOf(
            makeNode("n1", isLinked = true),
            makeNode("n2", isLinked = true),
        )
        assertTrue(GameEngine.isSolved(nodes))
    }

    @Test
    fun `isSolved - one unlinked node returns false`() {
        val nodes = listOf(
            makeNode("n1", isLinked = true),
            makeNode("n2", isLinked = false),
        )
        assertFalse(GameEngine.isSolved(nodes))
    }

    @Test
    fun `isSolved - empty list returns true`() {
        assertTrue(GameEngine.isSolved(emptyList()))
    }

    // ---- isCrescendo Tests ----

    @Test
    fun `isCrescendo - all linked and high energy is crescendo`() {
        val nodes = listOf(makeNode("n1", isLinked = true), makeNode("n2", isLinked = true))
        assertTrue(GameEngine.isCrescendo(nodes, 75f, 100f))
    }

    @Test
    fun `isCrescendo - all linked but low energy is not crescendo`() {
        val nodes = listOf(makeNode("n1", isLinked = true), makeNode("n2", isLinked = true))
        assertFalse(GameEngine.isCrescendo(nodes, 50f, 100f))
    }

    @Test
    fun `isCrescendo - unlinked nodes is not crescendo even with high energy`() {
        val nodes = listOf(makeNode("n1", isLinked = true), makeNode("n2", isLinked = false))
        assertFalse(GameEngine.isCrescendo(nodes, 90f, 100f))
    }

    // ---- Helpers ----

    private fun makeNode(id: String, isLinked: Boolean): Node = Node(
        id = id,
        colorType = NodeColor.VIOLET,
        position = Offset(0f, 0f),
        isLinked = isLinked,
    )
}

private fun assertEquals(expected: Float, actual: Float, absoluteTolerance: Float) {
    assertTrue(
        kotlin.math.abs(expected - actual) <= absoluteTolerance,
        "Expected $expected but was $actual (tolerance=$absoluteTolerance)"
    )
}
