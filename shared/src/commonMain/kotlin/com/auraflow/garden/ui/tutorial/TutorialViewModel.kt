package com.auraflow.garden.ui.tutorial

import androidx.lifecycle.ViewModel
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TutorialHint(
    val id: String,
    val text: String,
    val stageId: Int,
)

val TUTORIAL_HINTS = listOf(
    TutorialHint("t1", "Tap a node, then tap its matching color partner to connect them.", 1),
    TutorialHint("t2", "Connect all pairs to bloom the garden.", 2),
    TutorialHint("t3", "Links cannot cross — find a clear path.", 3),
    TutorialHint("t4", "Watch your energy — plan each connection.", 4),
    TutorialHint("t5", "The Warden's trial. Show your mastery.", 5),
)

class TutorialViewModel(private val settings: Settings) : ViewModel() {

    private val _currentHint = MutableStateFlow<TutorialHint?>(null)
    val currentHint: StateFlow<TutorialHint?> = _currentHint.asStateFlow()

    fun onStageStarted(stageId: Int) {
        if (stageId > 5) return  // Tutorial only for stages 1-5
        val hint = TUTORIAL_HINTS.find { it.stageId == stageId } ?: return
        if (hasShownHint(hint.id)) return
        _currentHint.value = hint
        // No auto-dismiss — stays until user taps or makes first move
    }

    fun onPlayerMadeFirstMove(stageId: Int) {
        val hint = _currentHint.value ?: return
        if (hint.stageId == stageId) {
            dismissHint(hint.id)
        }
    }

    fun dismissCurrentHint() {
        val hint = _currentHint.value ?: return
        dismissHint(hint.id)
    }

    private fun dismissHint(hintId: String) {
        markHintShown(hintId)
        _currentHint.value = null
    }

    private fun hasShownHint(hintId: String): Boolean =
        settings.getBoolean("tutorial_shown_$hintId", false)

    private fun markHintShown(hintId: String) {
        settings.putBoolean("tutorial_shown_$hintId", true)
    }

    fun resetAllTutorials() {
        TUTORIAL_HINTS.forEach { settings.putBoolean("tutorial_shown_${it.id}", false) }
    }
}
