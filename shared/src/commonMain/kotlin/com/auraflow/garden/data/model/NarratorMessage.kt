package com.auraflow.garden.data.model

import kotlinx.serialization.Serializable

@Serializable
data class NarratorMessage(
    val id: String,
    val speakerId: String,  // "warden", "luma", "system"
    val text: String,
    val emotion: String = "neutral",
    val durationMs: Long = 3000L,
    val priority: Int = 0,
)
