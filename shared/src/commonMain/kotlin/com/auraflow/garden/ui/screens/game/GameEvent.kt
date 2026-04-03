package com.auraflow.garden.ui.screens.game

import com.auraflow.garden.game.engagement.UnlockedAchievement

sealed class GameEvent {
    data object LinkDrawn : GameEvent()
    data object LinkFailed : GameEvent()
    data object LevelWon : GameEvent()
    data object LevelFailed : GameEvent()
    data object Crescendo : GameEvent()
    data class EnergyLow(val fraction: Float) : GameEvent()
    data class AchievementsUnlocked(val achievements: List<UnlockedAchievement>) : GameEvent()
    data class StreakUpdated(val days: Int) : GameEvent()
    data class StreakMilestone(val days: Int) : GameEvent()
    data object StreakReset : GameEvent()
    data class PressureExpired(val nodeId: String) : GameEvent()
}
