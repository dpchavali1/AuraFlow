package com.auraflow.garden.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerProgressDao {
    @Query("SELECT * FROM player_progress WHERE id = 1")
    fun observe(): Flow<PlayerProgress?>

    @Query("SELECT * FROM player_progress WHERE id = 1")
    suspend fun get(): PlayerProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: PlayerProgress)

    @Update
    suspend fun update(progress: PlayerProgress)
}
