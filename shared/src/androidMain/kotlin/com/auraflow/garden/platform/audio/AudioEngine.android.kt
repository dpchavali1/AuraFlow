package com.auraflow.garden.platform.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import com.auraflow.garden.data.model.NodeColor
import com.auraflow.garden.data.model.WorldType
import com.auraflow.garden.platform.PlatformContext
import kotlin.math.PI
import kotlin.math.sin

actual class AudioEngine(private val platformContext: PlatformContext) {

    private var sfxVolume = 1.0f
    private var musicVolume = 0.7f

    // Each NodeColor maps to a specific tone frequency (pentatonic-ish scale)
    private val colorFrequencies = mapOf(
        NodeColor.VIOLET to 261.63f,   // C4
        NodeColor.TEAL to 293.66f,     // D4
        NodeColor.ROSE to 329.63f,     // E4
        NodeColor.AMBER to 392.00f,    // G4
        NodeColor.CORAL to 440.00f,    // A4
        NodeColor.INDIGO to 523.25f,   // C5
        NodeColor.EMERALD to 587.33f,  // D5
        NodeColor.PEARL to 659.25f,    // E5
    )

    actual fun playLinkDrawn(nodeColor: NodeColor) {
        val freq = colorFrequencies[nodeColor] ?: 440f
        playTone(freq, 150)
    }

    actual fun playLinkFailed() {
        playTone(110f, 200)  // Low thud
    }

    actual fun playLevelWon() {
        // Play ascending triad
        playTone(523.25f, 150)
        playTone(659.25f, 150)
        playTone(783.99f, 300)
    }

    actual fun playLevelFailed() {
        playTone(130f, 400)  // Deep buzz
    }

    actual fun playCrescendo() {
        // Ascending glissando
        playTone(523.25f, 100)
        playTone(587.33f, 100)
        playTone(659.25f, 100)
        playTone(783.99f, 300)
    }

    actual fun playAmbient(worldType: WorldType) {
        // TODO Phase 10 extension: implement procedural ambient loop
        // For now: no-op (silent ambient)
    }

    actual fun stopAmbient() {
        // No-op for stub
    }

    actual fun setVolume(sfxVolume: Float, musicVolume: Float) {
        this.sfxVolume = sfxVolume
        this.musicVolume = musicVolume
    }

    actual fun release() {
        // No persistent resources to release in this stub
    }

    private fun playTone(frequencyHz: Float, durationMs: Int) {
        if (sfxVolume <= 0f) return
        try {
            val sampleRate = 44100
            val numSamples = sampleRate * durationMs / 1000
            val buffer = ShortArray(numSamples)

            val angularFrequency = 2.0 * PI * frequencyHz / sampleRate
            for (i in 0 until numSamples) {
                val envelope = when {
                    i < numSamples * 0.1 -> i / (numSamples * 0.1)  // attack
                    i > numSamples * 0.8 -> (numSamples - i) / (numSamples * 0.2)  // release
                    else -> 1.0
                }
                buffer[i] = (sin(angularFrequency * i) * Short.MAX_VALUE * 0.5 * envelope * sfxVolume).toInt().toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(numSamples * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, numSamples)
            audioTrack.play()

            // Release on a background thread after playback
            Thread {
                Thread.sleep(durationMs.toLong() + 50L)
                audioTrack.stop()
                audioTrack.release()
            }.start()
        } catch (e: Exception) {
            // Audio unavailable — continue silently
        }
    }
}
