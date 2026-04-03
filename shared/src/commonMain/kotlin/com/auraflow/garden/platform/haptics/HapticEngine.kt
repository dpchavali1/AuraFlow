package com.auraflow.garden.platform.haptics

/**
 * Cross-platform haptic feedback interface.
 * Android actual: VibrationEffect via Vibrator (API 26+)
 * iOS actual: UIImpactFeedbackGenerator / UINotificationFeedbackGenerator
 *
 * All methods are gated behind the haptics setting in GameViewModel.
 */
expect class HapticEngine {
    fun lightTap()      // node select
    fun mediumTap()     // link drawn
    fun heavyThud()     // link failed / level failed
    fun successRumble() // level won, crescendo
    fun warningBuzz()   // energy low warning
}
