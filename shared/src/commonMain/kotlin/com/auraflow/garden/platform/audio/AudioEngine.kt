package com.auraflow.garden.platform.audio

import com.auraflow.garden.data.model.NodeColor
import com.auraflow.garden.data.model.WorldType

/**
 * Cross-platform audio engine interface.
 * Android actual: SoundPool for SFX, MediaPlayer for ambient
 * iOS actual: AVFoundation stub (silence)
 *
 * Full generative music in a future phase.
 * All methods are safe to call even if audio is unavailable.
 */
expect class AudioEngine {
    fun playLinkDrawn(nodeColor: NodeColor)
    fun playLinkFailed()
    fun playLevelWon()
    fun playLevelFailed()
    fun playCrescendo()
    fun playAmbient(worldType: WorldType)
    fun stopAmbient()
    fun setVolume(sfxVolume: Float, musicVolume: Float)
    fun release()
}
