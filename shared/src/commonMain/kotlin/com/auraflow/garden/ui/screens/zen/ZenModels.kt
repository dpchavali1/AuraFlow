package com.auraflow.garden.ui.screens.zen

import androidx.compose.ui.geometry.Offset
import com.auraflow.garden.data.model.NodeColor
import kotlinx.serialization.Serializable

@Serializable
data class BlueprintNode(
    val id: String,
    val color: String,
    val x: Float,
    val y: Float,
    val pairedNodeId: String? = null,
)

@Serializable
data class BlueprintLink(
    val sourceId: String,
    val targetId: String,
    val color: String,
)

data class ZenUiState(
    val nodes: List<BlueprintNode> = emptyList(),
    val links: List<BlueprintLink> = emptyList(),
    val selectedColor: NodeColor = NodeColor.VIOLET,
    val gridSize: Int = 5,
    val isDirty: Boolean = false,
    val blueprintName: String = "My Garden",
    val savedBlueprintId: Long? = null,
)
