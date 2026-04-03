package com.auraflow.garden.data.model

import androidx.compose.ui.geometry.Offset

data class Link(
    val id: String,
    val sourceNodeId: String,
    val targetNodeId: String,
    val colorType: NodeColor,
    val pathPoints: List<Offset> = emptyList(),
    val energyCost: Float = 0f,
)
