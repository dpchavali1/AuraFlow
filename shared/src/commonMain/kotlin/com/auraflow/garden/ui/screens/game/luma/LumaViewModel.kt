package com.auraflow.garden.ui.screens.game.luma

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auraflow.garden.data.model.LumaEmotion
import com.auraflow.garden.data.model.LumaState
import com.auraflow.garden.ui.screens.game.GameEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LumaViewModel : ViewModel() {

    private val _lumaState = MutableStateFlow(LumaState())
    val lumaState: StateFlow<LumaState> = _lumaState.asStateFlow()

    private var idleJob: Job? = null
    private var transientJob: Job? = null
    private var lastEventTimeMs: Long = 0L

    init {
        startIdleTimer()
    }

    /**
     * Called when a game event occurs. Transitions Luma's emotion accordingly.
     */
    fun onGameEvent(event: GameEvent) {
        recordActivity()
        when (event) {
            is GameEvent.LinkDrawn -> setEmotion(LumaEmotion.HAPPY)
            is GameEvent.LinkFailed -> {
                setEmotion(LumaEmotion.SAD)
                scheduleReturnToIdle(2000L)
            }
            is GameEvent.LevelWon -> {
                setEmotion(LumaEmotion.CELEBRATING)
                scheduleReturnToEmotion(LumaEmotion.HAPPY, 3000L)
            }
            is GameEvent.LevelFailed -> setEmotion(LumaEmotion.SAD)
            is GameEvent.Crescendo -> setEmotion(LumaEmotion.EXCITED)
            is GameEvent.EnergyLow -> {
                if (event.fraction < 0.2f) {
                    setEmotion(LumaEmotion.WORRIED)
                }
            }
            is GameEvent.AchievementsUnlocked -> {
                setEmotion(LumaEmotion.CELEBRATING)
                scheduleReturnToEmotion(LumaEmotion.HAPPY, 3000L)
            }
            is GameEvent.StreakMilestone -> {
                setEmotion(LumaEmotion.EXCITED)
                scheduleReturnToIdle(3000L)
            }
            is GameEvent.StreakUpdated -> { /* no emotion change */ }
            is GameEvent.StreakReset -> { /* no emotion change */ }
            is GameEvent.PressureExpired -> {
                setEmotion(LumaEmotion.WORRIED)
                scheduleReturnToIdle(2000L)
            }
        }
    }

    private fun setEmotion(emotion: LumaEmotion) {
        transientJob?.cancel()
        // Show Luma and set the new emotion; schedule auto-hide after 3 seconds
        _lumaState.value = _lumaState.value.copy(emotion = emotion, isVisible = true)
        scheduleHide(3000L)
    }

    private fun scheduleHide(delayMs: Long) {
        transientJob?.cancel()
        transientJob = viewModelScope.launch {
            delay(delayMs)
            _lumaState.value = _lumaState.value.copy(isVisible = false, emotion = LumaEmotion.IDLE)
        }
    }

    private fun scheduleReturnToIdle(delayMs: Long) {
        scheduleHide(delayMs)
    }

    private fun scheduleReturnToEmotion(emotion: LumaEmotion, delayMs: Long) {
        transientJob?.cancel()
        transientJob = viewModelScope.launch {
            delay(delayMs)
            _lumaState.value = _lumaState.value.copy(emotion = emotion)
            scheduleHide(2000L)
        }
    }

    private fun recordActivity() {
        lastEventTimeMs = currentTimeMs()
        idleJob?.cancel()
    }

    private fun startIdleTimer() {
        // No-op: Luma is now event-driven only, not idle-timer-driven
    }

    private fun currentTimeMs(): Long = kotlin.time.Clock.System.now().toEpochMilliseconds()

    override fun onCleared() {
        super.onCleared()
        idleJob?.cancel()
        transientJob?.cancel()
    }
}
