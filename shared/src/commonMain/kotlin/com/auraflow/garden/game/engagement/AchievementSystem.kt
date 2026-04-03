package com.auraflow.garden.game.engagement

import com.auraflow.garden.data.local.PlayerProgress
import com.auraflow.garden.data.local.StageResult
import com.russhwolf.settings.Settings

enum class Achievement(
    val id: String,
    val title: String,
    val description: String,
) {
    FIRST_BLOOM("first_bloom", "First Bloom", "Complete your first stage"),
    PERFECT_MEADOW("perfect_meadow", "Perfect Meadow", "3-star all Whispering Meadow stages"),
    CRESCENDO_KING("crescendo_king", "Crescendo!", "Trigger 10 Crescendos"),
    STREAK_7("streak_7", "Week of Wonder", "7-day streak"),
    STREAK_30("streak_30", "Garden Keeper", "30-day streak"),
    SPEED_RUNNER("speed_runner", "Blink", "Complete any stage in under 15 seconds"),
}

data class UnlockedAchievement(
    val achievement: Achievement,
    val message: String,
)

class AchievementSystem(
    private val settings: Settings,
) {

    /**
     * Check and unlock any new achievements after a stage clear.
     * Returns list of newly unlocked achievements.
     */
    suspend fun checkAfterStageClear(
        result: StageResult,
        progress: PlayerProgress,
        allResults: List<StageResult>,
    ): List<UnlockedAchievement> {
        val newlyUnlocked = mutableListOf<UnlockedAchievement>()

        // FIRST_BLOOM — completed first stage
        if (result.stars >= 1 && !isUnlocked(Achievement.FIRST_BLOOM)) {
            unlock(Achievement.FIRST_BLOOM)
            newlyUnlocked.add(UnlockedAchievement(Achievement.FIRST_BLOOM, "You bloomed your first garden!"))
        }

        // SPEED_RUNNER — complete any stage in under 15 seconds
        if (result.elapsedMs < 15_000L && result.stars >= 1 && !isUnlocked(Achievement.SPEED_RUNNER)) {
            unlock(Achievement.SPEED_RUNNER)
            newlyUnlocked.add(UnlockedAchievement(Achievement.SPEED_RUNNER, "Lightning fast!"))
        }

        // CRESCENDO_KING — 10 total crescendos
        if (progress.totalCrescendos >= 10 && !isUnlocked(Achievement.CRESCENDO_KING)) {
            unlock(Achievement.CRESCENDO_KING)
            newlyUnlocked.add(UnlockedAchievement(Achievement.CRESCENDO_KING, "10 Crescendos achieved!"))
        }

        // STREAK_7 — 7-day streak
        if (progress.currentStreak >= 7 && !isUnlocked(Achievement.STREAK_7)) {
            unlock(Achievement.STREAK_7)
            newlyUnlocked.add(UnlockedAchievement(Achievement.STREAK_7, "7-day streak!"))
        }

        // STREAK_30 — 30-day streak
        if (progress.currentStreak >= 30 && !isUnlocked(Achievement.STREAK_30)) {
            unlock(Achievement.STREAK_30)
            newlyUnlocked.add(UnlockedAchievement(Achievement.STREAK_30, "30-day streak!"))
        }

        // PERFECT_MEADOW — 3-star all stages 1-5
        val meadowStageIds = (1..5).toList()
        val bestStarsByStage = meadowStageIds.associateWith { stageId ->
            allResults.filter { it.stageId == stageId }.maxOfOrNull { it.stars } ?: 0
        }
        if (meadowStageIds.all { (bestStarsByStage[it] ?: 0) >= 3 } && !isUnlocked(Achievement.PERFECT_MEADOW)) {
            unlock(Achievement.PERFECT_MEADOW)
            newlyUnlocked.add(UnlockedAchievement(Achievement.PERFECT_MEADOW, "Perfect Meadow!"))
        }

        return newlyUnlocked
    }

    fun isUnlocked(achievement: Achievement): Boolean =
        settings.getBoolean("achievement_${achievement.id}", false)

    private fun unlock(achievement: Achievement) {
        settings.putBoolean("achievement_${achievement.id}", true)
    }

    fun getAllUnlocked(): List<Achievement> =
        Achievement.entries.filter { isUnlocked(it) }
}
