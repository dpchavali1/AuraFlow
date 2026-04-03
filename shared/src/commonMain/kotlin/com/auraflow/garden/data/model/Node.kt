package com.auraflow.garden.data.model

import androidx.compose.ui.geometry.Offset

data class Node(
    val id: String,
    val colorType: NodeColor,
    val position: Offset,
    val isPressureNode: Boolean = false,
    val pressureDurationMs: Long = 0,
    val movementPath: List<Offset>? = null,
    val movementSpeedDps: Float = 0f,
    val isLinked: Boolean = false,
    val pairedNodeId: String = "",
)
