package com.auraflow.garden.platform.audio

import com.auraflow.garden.data.model.NodeColor
import com.auraflow.garden.data.model.WorldType

/**
 * iOS AudioEngine stub — silent implementation.
 * Full AVFoundation audio in a future phase.
 */
actual class AudioEngine {

    actual fun playLinkDrawn(nodeColor: NodeColor) {
        // TODO: AVAudioEngine tone generation in Phase 10 extension
    }

    actual fun playLinkFailed() {
        // TODO
    }

    actual fun playLevelWon() {
        // TODO
    }

    actual fun playLevelFailed() {
        // TODO
    }

    actual fun playCrescendo() {
        // TODO
    }

    actual fun playAmbient(worldType: WorldType) {
        // TODO
    }

    actual fun stopAmbient() {
        // TODO
    }

    actual fun setVolume(sfxVolume: Float, musicVolume: Float) {
        // TODO
    }

    actual fun release() {
        // No-op
    }
}
