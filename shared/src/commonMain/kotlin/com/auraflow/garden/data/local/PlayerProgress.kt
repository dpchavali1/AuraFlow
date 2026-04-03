package com.auraflow.garden.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_progress")
data class PlayerProgress(
    @PrimaryKey val id: Int = 1,
    val highestStageCleared: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastPlayedDate: String = "",
    val hasWardenPass: Boolean = false,
    val totalPerfectClears: Int = 0,
    val totalCrescendos: Int = 0,
    val selectedAuraSkin: String = "default",
    val selectedLumaSkin: String = "firefly",
    val wardenName: String = "Warden",
)
