package com.auraflow.garden.data.repository

import com.auraflow.garden.data.local.AuraFlowDatabase
import com.auraflow.garden.data.local.PlayerProgress
import com.auraflow.garden.data.local.StageResult
import com.auraflow.garden.data.local.UnlockedCosmeticEntity
import kotlinx.coroutines.flow.Flow

class PlayerRepository(private val database: AuraFlowDatabase) {

    fun observeProgress(): Flow<PlayerProgress?> =
        database.playerProgressDao().observe()

    suspend fun getProgress(): PlayerProgress =
        database.playerProgressDao().get() ?: PlayerProgress().also {
            database.playerProgressDao().upsert(it)
        }

    suspend fun updateProgress(progress: PlayerProgress) {
        database.playerProgressDao().upsert(progress)
    }

    suspend fun saveStageResult(result: StageResult) {
        database.stageResultDao().insert(result)
    }

    suspend fun getBestStars(stageId: Int): Int =
        database.stageResultDao().getBestStars(stageId) ?: 0

    suspend fun getClearedStageIds(): List<Int> =
        database.stageResultDao().getClearedStageIds()

    suspend fun getAllStageResults(): List<StageResult> =
        database.stageResultDao().getAll()

    fun observeCosmetics(): Flow<List<UnlockedCosmeticEntity>> =
        database.unlockedCosmeticDao().observeAll()

    suspend fun unlockCosmetic(cosmeticId: String, cosmeticType: String) {
        database.unlockedCosmeticDao().insert(
            UnlockedCosmeticEntity(
                cosmeticId = cosmeticId,
                cosmeticType = cosmeticType,
                unlockedAtMs = kotlin.time.Clock.System.now().toEpochMilliseconds(),
            )
        )
    }

    suspend fun isCosmeticUnlocked(cosmeticId: String): Boolean =
        database.unlockedCosmeticDao().isUnlocked(cosmeticId)

    suspend fun seedDefaults() {
        val progress = database.playerProgressDao().get()
        if (progress == null) {
            database.playerProgressDao().upsert(PlayerProgress())
            database.unlockedCosmeticDao().insert(
                UnlockedCosmeticEntity(cosmeticId = "default", cosmeticType = "aura_skin")
            )
            database.unlockedCosmeticDao().insert(
                UnlockedCosmeticEntity(cosmeticId = "firefly", cosmeticType = "luma_skin")
            )
        }
    }
}
