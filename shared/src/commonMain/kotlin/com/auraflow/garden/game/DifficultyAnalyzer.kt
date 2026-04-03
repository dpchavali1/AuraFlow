package com.auraflow.garden.game

import com.auraflow.garden.data.local.StageResult

data class DifficultyProfile(
    val averageCompletionRate: Float,  // 0-1
    val averageStars: Float,
    val averageTimeMs: Long,
    val recentFailCount: Int,
)

/**
 * Analyzes player performance to adjust difficulty.
 * Used by LevelRepository when constructing levels.
 */
class DifficultyAnalyzer {

    fun analyze(results: List<StageResult>): DifficultyProfile {
        if (results.isEmpty()) {
            return DifficultyProfile(
                averageCompletionRate = 0.5f,
                averageStars = 0f,
                averageTimeMs = 0L,
                recentFailCount = 0,
            )
        }

        val completedResults = results.filter { it.stars > 0 }
        val completionRate = completedResults.size.toFloat() / results.size.toFloat()
        val avgStars = if (completedResults.isNotEmpty()) {
            completedResults.sumOf { it.stars }.toFloat() / completedResults.size
        } else 0f
        val avgTime = if (completedResults.isNotEmpty()) {
            completedResults.sumOf { it.elapsedMs } / completedResults.size
        } else 0L

        // Count fails in the last 5 attempts
        val recentResults = results.takeLast(5)
        val recentFails = recentResults.count { it.stars == 0 }

        return DifficultyProfile(
            averageCompletionRate = completionRate,
            averageStars = avgStars,
            averageTimeMs = avgTime,
            recentFailCount = recentFails,
        )
    }

    fun shouldEaseNextStage(profile: DifficultyProfile): Boolean =
        profile.recentFailCount >= 3 || profile.averageCompletionRate < 0.4f

    fun shouldHardenNextStage(profile: DifficultyProfile): Boolean =
        profile.averageStars >= 2.8f && profile.averageCompletionRate > 0.9f

    fun adjustedEnergyMultiplier(base: Float, profile: DifficultyProfile): Float = when {
        shouldEaseNextStage(profile) -> base * 0.8f
        shouldHardenNextStage(profile) -> base * 1.15f
        else -> base
    }
}
