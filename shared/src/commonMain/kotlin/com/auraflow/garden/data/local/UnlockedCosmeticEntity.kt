package com.auraflow.garden.data.local

import androidx.room.Entity

@Entity(
    tableName = "unlocked_cosmetics",
    primaryKeys = ["playerId", "cosmeticId"],
)
data class UnlockedCosmeticEntity(
    val playerId: Int = 1,
    val cosmeticId: String,
    val cosmeticType: String,
    val unlockedAtMs: Long = 0L,
)
