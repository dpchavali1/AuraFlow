package com.auraflow.garden.platform.haptics

import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType

actual class HapticEngine {

    private val lightGenerator by lazy { UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleLight) }
    private val mediumGenerator by lazy { UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium) }
    private val heavyGenerator by lazy { UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy) }
    private val notificationGenerator by lazy { UINotificationFeedbackGenerator() }

    actual fun lightTap() {
        lightGenerator.impactOccurred()
    }

    actual fun mediumTap() {
        mediumGenerator.impactOccurred()
    }

    actual fun heavyThud() {
        heavyGenerator.impactOccurred()
    }

    actual fun successRumble() {
        notificationGenerator.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
    }

    actual fun warningBuzz() {
        notificationGenerator.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeWarning)
    }
}
