package com.auraflow.garden.platform.accessibility

import platform.UIKit.UIAccessibilityIsReduceMotionEnabled

actual fun isReduceMotionEnabled(): Boolean {
    return UIAccessibilityIsReduceMotionEnabled()
}
