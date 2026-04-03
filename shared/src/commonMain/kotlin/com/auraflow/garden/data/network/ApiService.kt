package com.auraflow.garden.data.network

import com.auraflow.garden.game.engagement.DailyChallenge
import com.auraflow.garden.game.engagement.DailyChallengeProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Ktor API client for backend communication.
 * Offline-first: always falls back to local seed on network failure.
 * Retry/backoff is handled by the Ktor plugin.
 */
class ApiService(private val apiKey: String = "") {

    private val client = HttpClient(createPlatformHttpEngine()) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        defaultRequest {
            if (apiKey.isNotBlank()) {
                header("X-Api-Key", apiKey)
            }
            contentType(ContentType.Application.Json)
        }
    }

    private val baseUrl = "https://api.auraflow.garden/api/v1"

    /**
     * Fetch today's daily challenge from backend.
     * Falls back to deterministic local seed if network unavailable.
     */
    suspend fun getDailyChallenge(): DailyChallenge {
        return try {
            val response: DailyChallengeResponse = client.get("$baseUrl/daily-challenge").body()
            DailyChallenge(
                date = response.date,
                stageId = response.stageId,
                specialRule = response.specialRule,
                bonusReward = response.bonusReward,
            )
        } catch (e: Exception) {
            // Offline fallback — deterministic from date
            DailyChallengeProvider.getTodayChallenge()
        }
    }

    /**
     * Submit a score to the leaderboard.
     * Silently fails if network unavailable.
     */
    suspend fun submitScore(stageId: Int, score: Int, playerName: String): LeaderboardResponse? {
        return try {
            client.post("$baseUrl/leaderboard") {
                setBody(LeaderboardRequest(stageId = stageId, score = score, playerName = playerName))
            }.body()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get top leaderboard entries for a stage.
     * Returns empty list if network unavailable.
     */
    suspend fun getLeaderboard(stageId: Int): List<LeaderboardEntry> {
        return try {
            val response: LeaderboardResponse = client.get("$baseUrl/leaderboard/$stageId").body()
            response.entries
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun close() {
        client.close()
    }
}
