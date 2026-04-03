package com.auraflow.garden.data.network

import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardEntry(
    val rank: Int,
    val playerName: String,
    val score: Int,
    val stageId: Int,
    val stars: Int,
)

@Serializable
data class DailyChallengeResponse(
    val date: String,
    val stageId: Int,
    val specialRule: String,
    val bonusReward: String,
    val serverSeed: Long,
)

@Serializable
data class LeaderboardRequest(
    val stageId: Int,
    val score: Int,
    val playerName: String,
)

@Serializable
data class LeaderboardResponse(
    val entries: List<LeaderboardEntry>,
    val playerRank: Int?,
)
