package com.auraflow.garden.ui.narrator

import com.auraflow.garden.data.model.NarratorMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages a priority queue of narrator messages.
 * Higher priority messages interrupt lower priority ones.
 * Messages auto-dismiss after durationMs.
 */
class NarratorQueue(private val scope: CoroutineScope) {

    private val _currentMessage = MutableStateFlow<NarratorMessage?>(null)
    val currentMessage: StateFlow<NarratorMessage?> = _currentMessage.asStateFlow()

    private val queue = mutableListOf<NarratorMessage>()
    private var dismissJob: Job? = null

    fun enqueue(message: NarratorMessage) {
        val current = _currentMessage.value
        if (current != null && message.priority <= current.priority) {
            // Add to queue for later if not higher priority
            queue.add(message)
            queue.sortByDescending { it.priority }
            return
        }
        // Higher priority — interrupt current
        if (current != null) {
            queue.add(0, current)
        }
        show(message)
    }

    fun dismiss() {
        dismissJob?.cancel()
        val nextMessage = queue.removeFirstOrNull()
        _currentMessage.value = nextMessage
        if (nextMessage != null) {
            scheduleDismiss(nextMessage.durationMs)
        }
    }

    fun clear() {
        dismissJob?.cancel()
        queue.clear()
        _currentMessage.value = null
    }

    private fun show(message: NarratorMessage) {
        dismissJob?.cancel()
        _currentMessage.value = message
        scheduleDismiss(message.durationMs)
    }

    private fun scheduleDismiss(durationMs: Long) {
        dismissJob = scope.launch {
            delay(durationMs)
            dismiss()
        }
    }
}
