package com.auraflow.garden.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Level(
    val stageId: Int,
    val world: WorldType,
    val title: String,
    val nodes: List<LevelNode>,
    val maxEnergy: Float,
    val energyCostMultiplier: Float = 1.0f,
    val parTime: Int = 60,
    val starThresholds: StarThresholds,
    val noIntersectionsRequired: Boolean = false,
    val narratorIntro: String? = null,
    val narratorOutro: String? = null,
)

@Serializable
data class LevelNode(
    val id: String,
    val color: String,
    val x: Float,
    val y: Float,
    val pairedNodeId: String,
    val isPressureNode: Boolean = false,
    val pressureDurationMs: Long = 0,
    val movementPath: List<LevelPoint>? = null,
    val movementSpeedDps: Float = 0f,
)

@Serializable
data class LevelPoint(val x: Float, val y: Float)

@Serializable
data class StarThresholds(
    val oneStar: Float,
    val twoStar: Float,
    val threeStar: Float,
)
