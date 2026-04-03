package com.auraflow.garden.ui.narrator

import com.auraflow.garden.data.model.NarratorMessage

/**
 * Warden personality strings — hardcoded for Phase 7, i18n in Phase 19.
 * All messages use the "warden" speaker ID.
 */
object NarratorMessages {

    fun firstLink() = NarratorMessage(
        id = "warden_first_link",
        speakerId = "warden",
        text = "Excellent. The garden remembers your touch.",
        emotion = "pleased",
        durationMs = 3000L,
        priority = 1,
    )

    fun invalidLink() = NarratorMessage(
        id = "warden_invalid_link",
        speakerId = "warden",
        text = "No... no.",
        emotion = "displeased",
        durationMs = 2000L,
        priority = 2,
    )

    fun energyLow() = NarratorMessage(
        id = "warden_energy_low",
        speakerId = "warden",
        text = "The energy fades. Choose wisely.",
        emotion = "concerned",
        durationMs = 3000L,
        priority = 3,
    )

    fun crescendo() = NarratorMessage(
        id = "warden_crescendo",
        speakerId = "warden",
        text = "Perfect clarity. A Crescendo.",
        emotion = "awed",
        durationMs = 4000L,
        priority = 5,
    )

    fun levelWon() = NarratorMessage(
        id = "warden_impressed",
        speakerId = "warden",
        text = "The Warden is... impressed.",
        emotion = "pleased",
        durationMs = 4000L,
        priority = 4,
    )

    fun levelFailed() = NarratorMessage(
        id = "warden_failed",
        speakerId = "warden",
        text = "The garden withers. Try again.",
        emotion = "somber",
        durationMs = 3000L,
        priority = 4,
    )

    fun stageIntro(text: String) = NarratorMessage(
        id = "stage_intro",
        speakerId = "warden",
        text = text,
        emotion = "neutral",
        durationMs = 4000L,
        priority = 1,
    )

    fun stageOutro(text: String) = NarratorMessage(
        id = "stage_outro",
        speakerId = "warden",
        text = text,
        emotion = "pleased",
        durationMs = 4000L,
        priority = 4,
    )

    fun streakMilestone(days: Int) = NarratorMessage(
        id = "streak_milestone_$days",
        speakerId = "warden",
        text = "A $days-day streak. Remarkable dedication.",
        emotion = "awed",
        durationMs = 4000L,
        priority = 3,
    )
}
