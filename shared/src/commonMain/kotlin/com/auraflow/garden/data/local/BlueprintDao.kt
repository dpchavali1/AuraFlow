package com.auraflow.garden.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BlueprintDao {
    @Query("SELECT * FROM blueprints ORDER BY updatedAtMs DESC")
    fun observeAll(): Flow<List<BlueprintEntity>>

    @Query("SELECT * FROM blueprints WHERE id = :id")
    suspend fun getById(id: Long): BlueprintEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(blueprint: BlueprintEntity): Long

    @Update
    suspend fun update(blueprint: BlueprintEntity)

    @Delete
    suspend fun delete(blueprint: BlueprintEntity)

    @Query("DELETE FROM blueprints")
    suspend fun deleteAll()
}
