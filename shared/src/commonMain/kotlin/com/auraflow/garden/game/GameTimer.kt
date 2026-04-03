package com.auraflow.garden.game

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlin.time.Clock

/**
 * Tracks elapsed time and emits tick events every 100ms.
 * Flow emits elapsed milliseconds since the timer started.
 */
class GameTimer {

    /**
     * Returns a Flow<Long> that emits elapsed milliseconds since start,
     * ticking every 100ms while the coroutine is active.
     */
    fun ticker(): Flow<Long> = flow {
        val startMs = Clock.System.now().toEpochMilliseconds()
        while (currentCoroutineContext().isActive) {
            val elapsed = Clock.System.now().toEpochMilliseconds() - startMs
            emit(elapsed)
            delay(100L)
        }
    }
}
