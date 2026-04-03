package com.auraflow.garden.data.model

data class LumaState(
    val skinId: String = "firefly",
    val emotion: LumaEmotion = LumaEmotion.IDLE,
    val isVisible: Boolean = false,  // hidden by default; shown transiently on game events
    val positionX: Float = 0.5f,  // normalized 0-1
    val positionY: Float = 0.8f,
)

enum class LumaEmotion { IDLE, HAPPY, EXCITED, WORRIED, SAD, THINKING, CELEBRATING }
