package com.auraflow.garden.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A saved Zen Mode blueprint.
 * nodesJson and linksJson are serialized as JSON strings (kotlinx-serialization).
 */
@Entity(tableName = "blueprints")
data class BlueprintEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val nodesJson: String,   // JSON array of BlueprintNode
    val linksJson: String,   // JSON array of BlueprintLink
    val gridSize: Int = 5,
    val createdAtMs: Long,
    val updatedAtMs: Long,
    val thumbnailData: ByteArray? = null,
)
