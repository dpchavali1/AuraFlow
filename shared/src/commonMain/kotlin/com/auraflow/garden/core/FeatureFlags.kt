package com.auraflow.garden.core

/**
 * Runtime feature toggles.
 * GIFT_ENABLED requires backend (Phase 18). Keep false until then.
 */
object FeatureFlags {
    const val GIFT_ENABLED = false
    const val DAILY_CHALLENGE_ENABLED = true
    const val ZEN_MODE_ENABLED = false  // Phase 16
    const val COMMUNITY_BLUEPRINTS_ENABLED = false  // Phase 16
    const val SHARE_CRESCENDO_ENABLED = false  // Phase 17
    const val LEADERBOARD_ENABLED = false  // Phase 18
}
