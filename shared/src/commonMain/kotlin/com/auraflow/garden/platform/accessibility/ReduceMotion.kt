package com.auraflow.garden.platform.accessibility

/**
 * Platform-specific Reduce Motion detection.
 * Android: Settings.Global.ANIMATOR_DURATION_SCALE == 0
 * iOS: UIAccessibility.isReduceMotionEnabled
 *
 * When active:
 * - Disable particles
 * - Disable sky/canvas animations
 * - Use instant transitions (no spring/fade)
 * - Keep haptics (unrelated to motion)
 */
expect fun isReduceMotionEnabled(): Boolean
