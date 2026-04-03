package com.auraflow.garden.game

import com.auraflow.garden.data.local.StageResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DifficultyAnalyzerTest {

    private val analyzer = DifficultyAnalyzer()

    private fun makeResult(stars: Int, elapsedMs: Long = 30_000L, stageId: Int = 1) = StageResult(
        stageId = stageId,
        stars = stars,
        score = if (stars > 0) 800 else 0,
        energyRemaining = if (stars > 0) 50f else 0f,
        elapsedMs = elapsedMs,
        isPerfectClear = stars == 3,
        isCrescendo = false,
        completedAtMs = 0L,
    )

    @Test
    fun `empty results returns default profile`() {
        val profile = analyzer.analyze(emptyList())
        assertEquals(0.5f, profile.averageCompletionRate)
        assertEquals(0f, profile.averageStars)
        assertEquals(0L, profile.averageTimeMs)
        assertEquals(0, profile.recentFailCount)
    }

    @Test
    fun `all completions gives 1f completion rate`() {
        val results = listOf(makeResult(3), makeResult(2), makeResult(1))
        val profile = analyzer.analyze(results)
        assertEquals(1f, profile.averageCompletionRate)
    }

    @Test
    fun `all failures gives 0f completion rate`() {
        val results = listOf(makeResult(0), makeResult(0), makeResult(0))
        val profile = analyzer.analyze(results)
        assertEquals(0f, profile.averageCompletionRate)
    }

    @Test
    fun `shouldEaseNextStage when 3+ recent fails`() {
        val results = (1..5).map { makeResult(0) }  // 5 fails
        val profile = analyzer.analyze(results)
        assertTrue(analyzer.shouldEaseNextStage(profile))
    }

    @Test
    fun `shouldEaseNextStage not triggered with 2 recent fails`() {
        val results = listOf(makeResult(3), makeResult(3), makeResult(0), makeResult(0))
        val profile = analyzer.analyze(results)
        assertFalse(analyzer.shouldEaseNextStage(profile))
    }

    @Test
    fun `shouldHardenNextStage when high stars and high completion`() {
        val results = (1..10).map { makeResult(3) }  // 10 three-star clears
        val profile = analyzer.analyze(results)
        assertTrue(analyzer.shouldHardenNextStage(profile))
    }

    @Test
    fun `shouldHardenNextStage not triggered when average stars low`() {
        val results = (1..5).map { makeResult(1) }
        val profile = analyzer.analyze(results)
        assertFalse(analyzer.shouldHardenNextStage(profile))
    }

    @Test
    fun `adjustedEnergyMultiplier reduces on struggling player`() {
        val results = (1..5).map { makeResult(0) }
        val profile = analyzer.analyze(results)
        val multiplier = analyzer.adjustedEnergyMultiplier(1f, profile)
        assertTrue(multiplier < 1f, "Expected <1f but got $multiplier")
    }

    @Test
    fun `adjustedEnergyMultiplier increases on excellent player`() {
        val results = (1..10).map { makeResult(3) }
        val profile = analyzer.analyze(results)
        val multiplier = analyzer.adjustedEnergyMultiplier(1f, profile)
        assertTrue(multiplier > 1f, "Expected >1f but got $multiplier")
    }

    @Test
    fun `adjustedEnergyMultiplier returns base for average player`() {
        val results = listOf(makeResult(2), makeResult(3), makeResult(1), makeResult(2))
        val profile = analyzer.analyze(results)
        val multiplier = analyzer.adjustedEnergyMultiplier(1f, profile)
        assertEquals(1f, multiplier)
    }

    @Test
    fun `recent fail count only counts last 5 attempts`() {
        // 4 fails at start, then 3 clears
        val results = listOf(makeResult(0), makeResult(0), makeResult(0), makeResult(0)) +
            listOf(makeResult(3), makeResult(3), makeResult(3))
        val profile = analyzer.analyze(results)
        // Last 5: fail, fail, 3-star, 3-star, 3-star → 2 recent fails
        assertEquals(2, profile.recentFailCount)
    }
}
