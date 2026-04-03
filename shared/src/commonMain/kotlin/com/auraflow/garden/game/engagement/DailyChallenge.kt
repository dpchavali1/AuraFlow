package com.auraflow.garden.game.engagement

import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable

@Serializable
data class DailyChallenge(
    val date: String,       // ISO date string, e.g. "2026-04-02"
    val stageId: Int,
    val specialRule: String,  // "no_undo", "speed_run", "minimal_energy"
    val bonusReward: String,
)

object DailyChallengeProvider {

    private val SPECIAL_RULES = listOf("no_undo", "speed_run", "minimal_energy")
    private val BONUS_REWARDS = listOf("100_gems", "150_gems", "aurora_skin_preview")

    /**
     * Returns today's daily challenge.
     * No backend required for Phase 13 — deterministic based on date.
     */
    fun getTodayChallenge(): DailyChallenge {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val dayOfYear = today.dayOfYear
        val stageId = (dayOfYear % 5) + 1
        val ruleIndex = dayOfYear % SPECIAL_RULES.size
        val rewardIndex = dayOfYear % BONUS_REWARDS.size

        return DailyChallenge(
            date = today.toString(),
            stageId = stageId,
            specialRule = SPECIAL_RULES[ruleIndex],
            bonusReward = BONUS_REWARDS[rewardIndex],
        )
    }
}
