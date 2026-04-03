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
    /** Normalized (0–1) countdown progress per unlinked pressure node; 0=fresh, 1=expired */
    val pressureProgress: Map<String, Float> = emptyMap(),
    /** Running total of link intersections drawn this attempt */
    val intersectionCount: Int = 0,
)

enum class GameStatus {
    LOADING,
    PLAYING,
    PAUSED,
    WON,
    FAILED,
}
