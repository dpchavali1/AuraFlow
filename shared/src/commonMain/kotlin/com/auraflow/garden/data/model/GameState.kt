package com.auraflow.garden.data.model

import androidx.compose.ui.geometry.Offset

data class GameState(
    val level: Level? = null,
    val nodes: List<Node> = emptyList(),
    val links: List<Link> = emptyList(),
    val energy: Float = 0f,
    val maxEnergy: Float = 0f,
    val score: Int = 0,
    val elapsedMs: Long = 0,
    val status: GameStatus = GameStatus.LOADING,
    val connectMode: Boolean = false,
    val selectedNodeId: String? = null,
    val crescendoActive: Boolean = false,
    val fizzleAt: Offset? = null,
)

enum class GameStatus {
    LOADING,
    PLAYING,
    PAUSED,
    WON,
    FAILED,
}
