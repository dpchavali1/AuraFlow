package com.auraflow.garden.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StageResultDao {
    @Query("SELECT * FROM stage_results WHERE stageId = :stageId ORDER BY score DESC LIMIT 1")
    suspend fun getBestResult(stageId: Int): StageResult?

    @Query("SELECT * FROM stage_results WHERE stageId = :stageId ORDER BY completedAtMs DESC")
    fun observeResults(stageId: Int): Flow<List<StageResult>>

    @Query("SELECT MAX(stars) FROM stage_results WHERE stageId = :stageId")
    suspend fun getBestStars(stageId: Int): Int?

    @Query("SELECT DISTINCT stageId FROM stage_results WHERE stars > 0 ORDER BY stageId")
    suspend fun getClearedStageIds(): List<Int>

    @Query("SELECT * FROM stage_results ORDER BY completedAtMs DESC")
    suspend fun getAll(): List<StageResult>

    @Insert
    suspend fun insert(result: StageResult)
}
