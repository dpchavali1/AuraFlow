package com.auraflow.garden.core.telemetry

/**
 * All analytics event names and parameter keys for AuraFlow.
 * Rules:
 *  - All events prefixed with "af_"
 *  - All names are constants — never use raw string literals in analytics calls
 *  - Include: player_id, stage_id, world_id, session_id on all events where applicable
 *  - No PII ever
 */
object AnalyticsEvents {

    // ── Navigation ────────────────────────────────────────────────────────────
    const val AF_HOME_VIEWED = "af_home_viewed"
    const val AF_GAME_STARTED = "af_game_started"
    const val AF_GAME_EXITED = "af_game_exited"

    // ── Gameplay ──────────────────────────────────────────────────────────────
    const val AF_STAGE_CLEARED = "af_stage_cleared"
    const val AF_STAGE_FAILED = "af_stage_failed"
    const val AF_NEAR_MISS = "af_near_miss"
    const val AF_PERFECT_CLEAR = "af_perfect_clear"
    const val AF_CRESCENDO = "af_crescendo"
    const val AF_LINK_DRAWN = "af_link_drawn"
    const val AF_LINK_UNDONE = "af_link_undone"
    const val AF_LUMA_BOOST_USED = "af_luma_boost_used"

    // ── Engagement ────────────────────────────────────────────────────────────
    const val AF_STREAK_STARTED = "af_streak_started"
    const val AF_STREAK_MILESTONE = "af_streak_milestone"
    const val AF_DAILY_CHALLENGE_STARTED = "af_daily_challenge_started"
    const val AF_DAILY_CHALLENGE_COMPLETED = "af_daily_challenge_completed"
    const val AF_ACHIEVEMENT_UNLOCKED = "af_achievement_unlocked"
    const val AF_NOTIFICATION_OPTED_IN = "af_notification_opted_in"
    const val AF_NOTIFICATION_OPTED_OUT = "af_notification_opted_out"

    // ── Monetization ──────────────────────────────────────────────────────────
    const val AF_STORE_VIEWED = "af_store_viewed"
    const val AF_PURCHASE_INITIATED = "af_purchase_initiated"
    const val AF_PURCHASE_COMPLETED = "af_purchase_completed"
    const val AF_PURCHASE_FAILED = "af_purchase_failed"
    const val AF_WARDEN_PASS_GATE_HIT = "af_warden_pass_gate_hit"

    // ── Ads ───────────────────────────────────────────────────────────────────
    const val AF_AD_OFFERED = "af_ad_offered"
    const val AF_AD_WATCHED = "af_ad_watched"
    const val AF_AD_DISMISSED = "af_ad_dismissed"
    const val AF_AD_FAILED_TO_LOAD = "af_ad_failed_to_load"

    // ── Parameter Keys ────────────────────────────────────────────────────────
    object Params {
        const val PLAYER_ID = "player_id"
        const val STAGE_ID = "stage_id"
        const val WORLD_ID = "world_id"
        const val SESSION_ID = "session_id"
        const val SCORE = "score"
        const val STARS = "stars"
        const val ENERGY_REMAINING = "energy_remaining"
        const val DIFFICULTY_LEVEL = "difficulty_level"
        const val PRODUCT_ID = "product_id"
        const val ACHIEVEMENT_ID = "achievement_id"
        const val DAYS = "days"
        const val ERROR_CODE = "error_code"
    }
}
