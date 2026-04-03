package com.auraflow.garden.game

import com.auraflow.garden.data.repository.LevelRepository
import com.auraflow.garden.data.repository.PlayerRepository

/**
 * Manages stage unlocking and progression.
 * Stage N+1 is unlocked when stage N has at least 1 star.
 */
class StageManager(
    private val levelRepository: LevelRepository,
    private val playerRepository: PlayerRepository,
) {

    private val totalStages = 50  // 1-15: Meadow, 16-30: Caverns, 31-45: Floating Isles, 46-50: Deep Sea

    /**
     * Returns true if the given stage is unlocked.
     * Stage 1 is always unlocked.
     * Stage N+1 is unlocked when stage N has been cleared (≥1 star).
     */
    suspend fun isStageUnlocked(stageId: Int): Boolean {
        if (stageId <= 1) return true
        return playerRepository.getBestStars(stageId - 1) >= 1
    }

    /**
     * Returns the best star count for each stage, used to display the stage grid.
     */
    suspend fun getStageProgressList(): List<StageProgress> {
        return (1..totalStages).map { stageId ->
            StageProgress(
                stageId = stageId,
                isUnlocked = isStageUnlocked(stageId),
                bestStars = playerRepository.getBestStars(stageId),
            )
        }
    }

    /**
     * Returns the next stage ID, or null if all stages are complete.
     */
    fun nextStage(currentStageId: Int): Int? {
        return if (currentStageId < totalStages) currentStageId + 1 else null
    }

    /**
     * Returns the previous stage ID, or null if at first stage.
     */
    fun previousStage(currentStageId: Int): Int? {
        return if (currentStageId > 1) currentStageId - 1 else null
    }
}

data class StageProgress(
    val stageId: Int,
    val isUnlocked: Boolean,
    val bestStars: Int,  // 0-3
)
