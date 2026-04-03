package com.auraflow.garden.game.engagement

import com.auraflow.garden.data.local.PlayerProgress
import com.auraflow.garden.data.repository.PlayerRepository
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

sealed class StreakEvent {
    data class StreakUpdated(val streakDays: Int) : StreakEvent()
    data class StreakMilestone(val days: Int) : StreakEvent()
    data object StreakReset : StreakEvent()
}

private val MILESTONE_DAYS = setOf(3, 7, 14, 30)

class StreakManager(private val playerRepository: PlayerRepository) {

    /**
     * Call after a stage clear. Updates streak based on today's date.
     * Returns the streak event that occurred.
     */
    suspend fun onStageClear(): StreakEvent {
        val progress = playerRepository.getProgress()
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val todayStr = today.toString()

        if (progress.lastPlayedDate == todayStr) {
            // Already played today — no streak change
            return StreakEvent.StreakUpdated(progress.currentStreak)
        }

        val lastDate = if (progress.lastPlayedDate.isBlank()) {
            null
        } else {
            try {
                LocalDate.parse(progress.lastPlayedDate)
            } catch (e: Exception) {
                null
            }
        }

        val yesterday = today.minus(1, DateTimeUnit.DAY)
        val newStreak = when {
            lastDate == null -> 1
            lastDate == yesterday -> progress.currentStreak + 1
            else -> 1  // Gap > 1 day — reset streak
        }

        val isReset = lastDate != null && lastDate != yesterday && progress.currentStreak > 0
        val newLongest = maxOf(progress.longestStreak, newStreak)

        playerRepository.updateProgress(
            progress.copy(
                currentStreak = newStreak,
                longestStreak = newLongest,
                lastPlayedDate = todayStr,
            )
        )

        if (isReset) return StreakEvent.StreakReset

        return if (newStreak in MILESTONE_DAYS) {
            StreakEvent.StreakMilestone(newStreak)
        } else {
            StreakEvent.StreakUpdated(newStreak)
        }
    }
}
