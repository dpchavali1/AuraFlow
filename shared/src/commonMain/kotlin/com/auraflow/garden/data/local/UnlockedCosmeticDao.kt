package com.auraflow.garden.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UnlockedCosmeticDao {
    @Query("SELECT * FROM unlocked_cosmetics WHERE playerId = :playerId")
    fun observeAll(playerId: Int = 1): Flow<List<UnlockedCosmeticEntity>>

    @Query("SELECT * FROM unlocked_cosmetics WHERE playerId = :playerId AND cosmeticType = :type")
    suspend fun getByType(type: String, playerId: Int = 1): List<UnlockedCosmeticEntity>

    @Query("SELECT COUNT(*) > 0 FROM unlocked_cosmetics WHERE playerId = :playerId AND cosmeticId = :cosmeticId")
    suspend fun isUnlocked(cosmeticId: String, playerId: Int = 1): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(cosmetic: UnlockedCosmeticEntity)
}
