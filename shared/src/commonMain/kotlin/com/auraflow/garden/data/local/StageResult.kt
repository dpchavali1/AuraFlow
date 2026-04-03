package com.auraflow.garden.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stage_results")
data class StageResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val stageId: Int,
    val stars: Int,
    val score: Int,
    val energyRemaining: Float,
    val elapsedMs: Long,
    val isPerfectClear: Boolean,
    val isCrescendo: Boolean,
    val completedAtMs: Long,
)
