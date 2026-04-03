package com.auraflow.garden.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class WorldType(val displayName: String, val stageRange: IntRange) {
    WHISPERING_MEADOW("Whispering Meadow", 1..15),
    CRYSTAL_CAVERNS("Crystal Caverns", 16..30),
    FLOATING_ISLES("Floating Isles", 31..45),
    DEEP_SEA("Deep Sea", 46..60),
    GLITCH_CITY("Glitch City", 61..75),
    CELESTIAL_SUMMIT("Celestial Summit", 76..90);

    val isPlayable: Boolean get() = stageRange.first <= 50
}
