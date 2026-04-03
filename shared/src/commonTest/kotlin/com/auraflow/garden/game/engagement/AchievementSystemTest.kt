package com.auraflow.garden.game.engagement

import com.auraflow.garden.data.local.PlayerProgress
import com.auraflow.garden.data.local.StageResult
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AchievementSystemTest {

    private fun makeResult(
        stageId: Int = 1,
        stars: Int = 3,
        elapsedMs: Long = 30_000L,
    ) = StageResult(
        stageId = stageId,
        stars = stars,
        score = 1000,
        energyRemaining = 50f,
        elapsedMs = elapsedMs,
        isPerfectClear = stars == 3,
        isCrescendo = false,
        completedAtMs = 0L,
    )

    private fun makeProgress(
        totalCrescendos: Int = 0,
        currentStreak: Int = 0,
    ) = PlayerProgress(
        totalCrescendos = totalCrescendos,
        currentStreak = currentStreak,
    )

    private fun makeSystem() = AchievementSystem(MapSettings())

    @Test
    fun `FIRST_BLOOM unlocked on first stage clear`() = runTest {
        val system = makeSystem()
        val result = makeResult(stars = 1)
        val unlocked = system.checkAfterStageClear(result, makeProgress(), emptyList())
        assertTrue(unlocked.any { it.achievement == Achievement.FIRST_BLOOM })
        assertTrue(system.isUnlocked(Achievement.FIRST_BLOOM))
    }

    @Test
    fun `FIRST_BLOOM not unlocked twice`() = runTest {
        val system = makeSystem()
        val result = makeResult(stars = 1)
        system.checkAfterStageClear(result, makeProgress(), emptyList())
        val second = system.checkAfterStageClear(result, makeProgress(), emptyList())
        assertFalse(second.any { it.achievement == Achievement.FIRST_BLOOM })
    }

    @Test
    fun `SPEED_RUNNER unlocked for sub-15s completion`() = runTest {
        val system = makeSystem()
        val result = makeResult(stars = 1, elapsedMs = 14_999L)
        val unlocked = system.checkAfterStageClear(result, makeProgress(), emptyList())
        assertTrue(unlocked.any { it.achievement == Achievement.SPEED_RUNNER })
    }

    @Test
    fun `SPEED_RUNNER not unlocked at exactly 15s`() = runTest {
        val system = makeSystem()
        val result = makeResult(stars = 1, elapsedMs = 15_000L)
        val unlocked = system.checkAfterStageClear(result, makeProgress(), emptyList())
        assertFalse(unlocked.any { it.achievement == Achievement.SPEED_RUNNER })
    }

    @Test
    fun `CRESCENDO_KING unlocked at 10 total crescendos`() = runTest {
        val system = makeSystem()
        val result = makeResult()
        val progress = makeProgress(totalCrescendos = 10)
        val unlocked = system.checkAfterStageClear(result, progress, emptyList())
        assertTrue(unlocked.any { it.achievement == Achievement.CRESCENDO_KING })
    }

    @Test
    fun `CRESCENDO_KING not unlocked at 9 crescendos`() = runTest {
        val system = makeSystem()
        val result = makeResult()
        val progress = makeProgress(totalCrescendos = 9)
        val unlocked = system.checkAfterStageClear(result, progress, emptyList())
        assertFalse(unlocked.any { it.achievement == Achievement.CRESCENDO_KING })
    }

    @Test
    fun `STREAK_7 unlocked at 7 day streak`() = runTest {
        val system = makeSystem()
        val result = makeResult()
        val progress = makeProgress(currentStreak = 7)
        val unlocked = system.checkAfterStageClear(result, progress, emptyList())
        assertTrue(unlocked.any { it.achievement == Achievement.STREAK_7 })
    }

    @Test
    fun `STREAK_30 unlocked at 30 day streak`() = runTest {
        val system = makeSystem()
        val result = makeResult()
        val progress = makeProgress(currentStreak = 30)
        val unlocked = system.checkAfterStageClear(result, progress, emptyList())
        assertTrue(unlocked.any { it.achievement == Achievement.STREAK_30 })
    }

    @Test
    fun `PERFECT_MEADOW unlocked when all stages 1-5 have 3 stars`() = runTest {
        val system = makeSystem()
        val result = makeResult(stageId = 5, stars = 3)
        val allResults = (1..5).map { makeResult(stageId = it, stars = 3) }
        val unlocked = system.checkAfterStageClear(result, makeProgress(), allResults)
        assertTrue(unlocked.any { it.achievement == Achievement.PERFECT_MEADOW })
    }

    @Test
    fun `PERFECT_MEADOW not unlocked if one stage has 2 stars`() = runTest {
        val system = makeSystem()
        val result = makeResult(stageId = 5, stars = 3)
        val allResults = (1..5).map { stageId ->
            makeResult(stageId = stageId, stars = if (stageId == 3) 2 else 3)
        }
        val unlocked = system.checkAfterStageClear(result, makeProgress(), allResults)
        assertFalse(unlocked.any { it.achievement == Achievement.PERFECT_MEADOW })
    }

    @Test
    fun `getAllUnlocked returns empty list when nothing unlocked`() {
        val system = makeSystem()
        assertTrue(system.getAllUnlocked().isEmpty())
    }

    @Test
    fun `achievement condition fires once not repeatedly`() = runTest {
        val system = makeSystem()
        val result = makeResult(stars = 1)
        val progress = makeProgress(totalCrescendos = 10, currentStreak = 7)
        system.checkAfterStageClear(result, progress, emptyList())
        val second = system.checkAfterStageClear(result, progress, emptyList())
        // All achievements already unlocked — second call returns empty
        assertTrue(second.none { it.achievement in listOf(Achievement.FIRST_BLOOM, Achievement.CRESCENDO_KING, Achievement.STREAK_7) })
    }
}
