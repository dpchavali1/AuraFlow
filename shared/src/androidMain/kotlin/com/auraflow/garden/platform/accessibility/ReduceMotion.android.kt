package com.auraflow.garden.platform.accessibility

import android.provider.Settings
import com.auraflow.garden.platform.PlatformContext

// PlatformContext is accessed via global Koin instance for this utility function.
// The expected pattern is to call this at composable entry, not in loops.
private var _platformContext: PlatformContext? = null

fun initReduceMotion(context: PlatformContext) {
    _platformContext = context
}

actual fun isReduceMotionEnabled(): Boolean {
    val context = _platformContext?.context ?: return false
    return try {
        val scale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        )
        scale == 0f
    } catch (e: Exception) {
        false
    }
}
