package com.auraflow.garden.data.local

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection

/**
 * Room database migrations.
 * NEVER use fallbackToDestructiveMigration() — always write explicit migrations.
 * Room KMP 2.8.x: use connection.prepare(...).use { it.step() } instead of execSQL.
 */
object DatabaseMigrations {

    /**
     * v1 → v2: Add blueprints table (Phase 16 Zen Mode)
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(connection: SQLiteConnection) {
            connection.prepare(
                """
                CREATE TABLE IF NOT EXISTS `blueprints` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `nodesJson` TEXT NOT NULL,
                    `linksJson` TEXT NOT NULL,
                    `gridSize` INTEGER NOT NULL DEFAULT 5,
                    `createdAtMs` INTEGER NOT NULL,
                    `updatedAtMs` INTEGER NOT NULL,
                    `thumbnailData` BLOB
                )
                """.trimIndent()
            ).use { it.step() }
        }
    }
}
