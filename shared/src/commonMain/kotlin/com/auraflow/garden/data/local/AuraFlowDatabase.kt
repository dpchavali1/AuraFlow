package com.auraflow.garden.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [
        PlayerProgress::class,
        StageResult::class,
        UnlockedCosmeticEntity::class,
        BlueprintEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@ConstructedBy(AuraFlowDatabaseConstructor::class)
abstract class AuraFlowDatabase : RoomDatabase() {
    abstract fun playerProgressDao(): PlayerProgressDao
    abstract fun stageResultDao(): StageResultDao
    abstract fun unlockedCosmeticDao(): UnlockedCosmeticDao
    abstract fun blueprintDao(): BlueprintDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AuraFlowDatabaseConstructor : RoomDatabaseConstructor<AuraFlowDatabase> {
    override fun initialize(): AuraFlowDatabase
}
