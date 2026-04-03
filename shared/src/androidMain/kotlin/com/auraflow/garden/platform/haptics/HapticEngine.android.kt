package com.auraflow.garden.platform.haptics

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.auraflow.garden.platform.PlatformContext

actual class HapticEngine(private val platformContext: PlatformContext) {

    private val vibrator: Vibrator by lazy {
        val context = platformContext.context
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    actual fun lightTap() {
        vibrator.vibrate(VibrationEffect.createOneShot(20L, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    actual fun mediumTap() {
        vibrator.vibrate(VibrationEffect.createOneShot(40L, 128))
    }

    actual fun heavyThud() {
        vibrator.vibrate(VibrationEffect.createOneShot(80L, 255))
    }

    actual fun successRumble() {
        val pattern = VibrationEffect.createWaveform(
            longArrayOf(0L, 50L, 50L, 100L),
            intArrayOf(0, 180, 0, 255),
            -1,
        )
        vibrator.vibrate(pattern)
    }

    actual fun warningBuzz() {
        val pattern = VibrationEffect.createWaveform(
            longArrayOf(0L, 30L, 30L, 30L),
            intArrayOf(0, 200, 0, 200),
            -1,
        )
        vibrator.vibrate(pattern)
    }
}
