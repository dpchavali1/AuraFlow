package com.auraflow.garden.ui.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auraflow.garden.data.local.StageResult
import com.auraflow.garden.data.model.GameState
import com.auraflow.garden.data.model.GameStatus
import com.auraflow.garden.data.model.Link
import com.auraflow.garden.data.repository.LevelRepository
import com.auraflow.garden.data.repository.PlayerRepository
import com.auraflow.garden.data.settings.AppSettings
import com.auraflow.garden.game.GameEngine
import com.auraflow.garden.game.engagement.AchievementSystem
import com.auraflow.garden.game.engagement.StreakEvent
import com.auraflow.garden.game.engagement.StreakManager
import com.auraflow.garden.platform.audio.AudioEngine
import com.auraflow.garden.platform.haptics.HapticEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock

class GameViewModel(
    private val levelRepository: LevelRepository,
    private val playerRepository: PlayerRepository,
    private val hapticEngine: HapticEngine,
    private val appSettings: AppSettings,
    private val audioEngine: AudioEngine,
    private val streakManager: StreakManager,
    private val achievementSystem: AchievementSystem,
) : ViewModel() {

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<GameEvent>()
    val events: SharedFlow<GameEvent> = _events.asSharedFlow()

    private var timerJob: Job? = null
    private var startTimeMs: Long = 0L
    private var energyWarningSent = false

    fun loadStage(stageId: Int) {
        val level = levelRepository.loadLevel(stageId)
        val nodes = levelRepository.levelToNodes(level)
        _state.value = GameState(
            level = level,
            nodes = nodes,
            links = emptyList(),
            energy = level.maxEnergy,
            maxEnergy = level.maxEnergy,
            score = 0,
            elapsedMs = 0L,
            status = GameStatus.PLAYING,
            connectMode = false,
            selectedNodeId = null,
        )
        energyWarningSent = false
        audioEngine.setVolume(appSettings.sfxVolume, appSettings.musicVolume)
        level.world.let { audioEngine.playAmbient(it) }
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        startTimeMs = Clock.System.now().toEpochMilliseconds()
        timerJob = viewModelScope.launch {
            while (_state.value.status == GameStatus.PLAYING) {
                delay(100L)
                val now = Clock.System.now().toEpochMilliseconds()
                val elapsed = now - startTimeMs
                _state.value = _state.value.copy(elapsedMs = elapsed)
            }
        }
    }

    fun onNodeTapped(nodeId: String) {
        val currentState = _state.value
        if (currentState.status != GameStatus.PLAYING) return

        haptic { lightTap() }

        if (!currentState.connectMode) {
            _state.value = currentState.copy(connectMode = true, selectedNodeId = nodeId)
            return
        }

        val selectedId = currentState.selectedNodeId
        if (selectedId == null) {
            _state.value = currentState.copy(selectedNodeId = nodeId)
            return
        }
        if (selectedId == nodeId) {
            _state.value = currentState.copy(selectedNodeId = null)
            return
        }
        attemptLink(selectedId, nodeId)
        _state.value = _state.value.copy(selectedNodeId = null)
    }

    fun onDragStart(nodeId: String) {
        val currentState = _state.value
        if (currentState.status != GameStatus.PLAYING) return
        _state.value = currentState.copy(selectedNodeId = nodeId)
    }

    fun onDragEnd(targetNodeId: String?) {
        val currentState = _state.value
        if (currentState.status != GameStatus.PLAYING) return
        val sourceId = currentState.selectedNodeId ?: return
        if (targetNodeId != null && targetNodeId != sourceId) {
            attemptLink(sourceId, targetNodeId)
        }
        _state.value = _state.value.copy(selectedNodeId = null)
    }

    private fun attemptLink(sourceId: String, targetId: String) {
        val currentState = _state.value
        val nodes = currentState.nodes
        val sourceNode = nodes.find { it.id == sourceId } ?: return
        val targetNode = nodes.find { it.id == targetId } ?: return

        // Validate: same color pair
        if (sourceNode.colorType != targetNode.colorType) {
            viewModelScope.launch {
                haptic { heavyThud() }
                audioEngine.playLinkFailed()
                _events.emit(GameEvent.LinkFailed)
            }
            return
        }
        // Validate: correct pair
        if (sourceNode.pairedNodeId != targetId) {
            viewModelScope.launch {
                haptic { heavyThud() }
                audioEngine.playLinkFailed()
                _events.emit(GameEvent.LinkFailed)
            }
            return
        }
        // Validate: neither already linked
        if (sourceNode.isLinked || targetNode.isLinked) {
            viewModelScope.launch {
                haptic { heavyThud() }
                audioEngine.playLinkFailed()
                _events.emit(GameEvent.LinkFailed)
            }
            return
        }

        // Calculate energy cost using GameEngine
        val energyCost = GameEngine.calculateEnergyCost(
            sourceNode.position,
            targetNode.position,
            currentState.level?.energyCostMultiplier ?: 1f,
        )
        if (energyCost > currentState.energy) {
            viewModelScope.launch {
                haptic { warningBuzz() }
                _events.emit(GameEvent.LinkFailed)
            }
            return
        }

        // Check intersections if required using GameEngine
        if (currentState.level?.noIntersectionsRequired == true) {
            val wouldIntersect = currentState.links.any { existingLink ->
                val linkSourceNode = nodes.find { it.id == existingLink.sourceNodeId }
                val linkTargetNode = nodes.find { it.id == existingLink.targetNodeId }
                if (linkSourceNode == null || linkTargetNode == null) return@any false
                if (existingLink.sourceNodeId == sourceId || existingLink.targetNodeId == sourceId ||
                    existingLink.sourceNodeId == targetId || existingLink.targetNodeId == targetId
                ) return@any false
                GameEngine.checkIntersection(
                    sourceNode.position, targetNode.position,
                    linkSourceNode.position, linkTargetNode.position,
                )
            }
            if (wouldIntersect) {
                viewModelScope.launch {
                    haptic { heavyThud() }
                    _events.emit(GameEvent.LinkFailed)
                }
                return
            }
        }

        // Draw the link
        val newLink = Link(
            id = "${sourceId}_${targetId}",
            sourceNodeId = sourceId,
            targetNodeId = targetId,
            colorType = sourceNode.colorType,
            pathPoints = listOf(sourceNode.position, targetNode.position),
            energyCost = energyCost,
        )
        val newEnergy = (currentState.energy - energyCost).coerceAtLeast(0f)
        val updatedNodes = nodes.map { node ->
            when (node.id) {
                sourceId, targetId -> node.copy(isLinked = true)
                else -> node
            }
        }
        val level = currentState.level
        val newScore = if (level != null && GameEngine.isSolved(updatedNodes)) {
            GameEngine.calculateScore(
                energyRemaining = newEnergy,
                maxEnergy = currentState.maxEnergy,
                elapsedMs = currentState.elapsedMs,
                parTimeMs = level.parTime * 1000L,
            )
        } else {
            currentState.score
        }

        _state.value = currentState.copy(
            nodes = updatedNodes,
            links = currentState.links + newLink,
            energy = newEnergy,
            score = newScore,
        )

        viewModelScope.launch {
            haptic { mediumTap() }
            audioEngine.playLinkDrawn(sourceNode.colorType)
            _events.emit(GameEvent.LinkDrawn)

            if (GameEngine.isSolved(updatedNodes)) {
                val isCrescendo = GameEngine.isCrescendo(updatedNodes, newEnergy, currentState.maxEnergy)
                if (isCrescendo) {
                    haptic { successRumble() }
                    audioEngine.playCrescendo()
                    _state.value = _state.value.copy(crescendoActive = true)
                    _events.emit(GameEvent.Crescendo)
                } else {
                    haptic { successRumble() }
                    audioEngine.playLevelWon()
                }
                _state.value = _state.value.copy(status = GameStatus.WON)
                _events.emit(GameEvent.LevelWon)
                timerJob?.cancel()
                // Save result and check engagement
                val level = currentState.level
                if (level != null) {
                    val stars = GameEngine.calculateStars(newEnergy, currentState.maxEnergy, level.starThresholds)
                    saveStageResult(
                        stageId = level.stageId,
                        stars = stars,
                        score = newScore,
                        energyRemaining = newEnergy,
                        elapsedMs = _state.value.elapsedMs,
                        isPerfectClear = stars == 3,
                        isCrescendo = isCrescendo,
                    )
                }
                return@launch
            }

            // Check energy low warning
            val energyFraction = newEnergy / currentState.maxEnergy.coerceAtLeast(1f)
            if (energyFraction <= 0.1f && !energyWarningSent) {
                energyWarningSent = true
                haptic { warningBuzz() }
                _events.emit(GameEvent.EnergyLow(energyFraction))
            }

            // Check fail — energy is 0 and nodes remain unlinked
            if (newEnergy <= 0f && !GameEngine.isSolved(updatedNodes)) {
                haptic { heavyThud() }
                audioEngine.playLevelFailed()
                _state.value = _state.value.copy(status = GameStatus.FAILED)
                _events.emit(GameEvent.LevelFailed)
                timerJob?.cancel()
                // Save fail result
                val level = currentState.level
                if (level != null) {
                    saveStageResult(
                        stageId = level.stageId,
                        stars = 0,
                        score = currentState.score,
                        energyRemaining = newEnergy,
                        elapsedMs = _state.value.elapsedMs,
                        isPerfectClear = false,
                        isCrescendo = false,
                    )
                }
            }
        }
    }

    private fun saveStageResult(
        stageId: Int,
        stars: Int,
        score: Int,
        energyRemaining: Float,
        elapsedMs: Long,
        isPerfectClear: Boolean,
        isCrescendo: Boolean,
    ) {
        viewModelScope.launch {
            val result = StageResult(
                stageId = stageId,
                stars = stars,
                score = score,
                energyRemaining = energyRemaining,
                elapsedMs = elapsedMs,
                isPerfectClear = isPerfectClear,
                isCrescendo = isCrescendo,
                completedAtMs = kotlin.time.Clock.System.now().toEpochMilliseconds(),
            )
            playerRepository.saveStageResult(result)

            if (stars >= 1) {
                // Update progress stats
                val progress = playerRepository.getProgress()
                val newTotalCrescendos = if (isCrescendo) progress.totalCrescendos + 1 else progress.totalCrescendos
                val newTotalPerfect = if (isPerfectClear) progress.totalPerfectClears + 1 else progress.totalPerfectClears
                val newHighest = maxOf(progress.highestStageCleared, stageId)
                val updatedProgress = progress.copy(
                    highestStageCleared = newHighest,
                    totalCrescendos = newTotalCrescendos,
                    totalPerfectClears = newTotalPerfect,
                )
                playerRepository.updateProgress(updatedProgress)

                // Update streak
                val streakEvent = streakManager.onStageClear()
                when (streakEvent) {
                    is StreakEvent.StreakUpdated -> _events.emit(GameEvent.StreakUpdated(streakEvent.streakDays))
                    is StreakEvent.StreakMilestone -> _events.emit(GameEvent.StreakMilestone(streakEvent.days))
                    is StreakEvent.StreakReset -> _events.emit(GameEvent.StreakReset)
                }

                // Check achievements
                val freshProgress = playerRepository.getProgress()
                val allStageResults = playerRepository.getAllStageResults()
                val newAchievements = achievementSystem.checkAfterStageClear(result, freshProgress, allStageResults)
                if (newAchievements.isNotEmpty()) {
                    _events.emit(GameEvent.AchievementsUnlocked(newAchievements))
                }
            }
        }
    }

    fun undoLastLink() {
        val currentState = _state.value
        if (currentState.status != GameStatus.PLAYING) return
        val links = currentState.links
        if (links.isEmpty()) return

        val lastLink = links.last()
        val restoredEnergy = currentState.energy + lastLink.energyCost
        val updatedNodes = currentState.nodes.map { node ->
            if (node.id == lastLink.sourceNodeId || node.id == lastLink.targetNodeId) {
                node.copy(isLinked = false)
            } else {
                node
            }
        }
        _state.value = currentState.copy(
            links = links.dropLast(1),
            nodes = updatedNodes,
            energy = restoredEnergy.coerceAtMost(currentState.maxEnergy),
        )
        energyWarningSent = restoredEnergy <= currentState.maxEnergy * 0.1f
    }

    fun undoAll() {
        val currentState = _state.value
        if (currentState.status != GameStatus.PLAYING) return
        val resetNodes = currentState.nodes.map { it.copy(isLinked = false) }
        _state.value = currentState.copy(
            links = emptyList(),
            nodes = resetNodes,
            energy = currentState.maxEnergy,
        )
        energyWarningSent = false
    }

    fun toggleConnectMode() {
        val currentState = _state.value
        _state.value = currentState.copy(
            connectMode = !currentState.connectMode,
            selectedNodeId = null,
        )
    }

    fun pauseGame() {
        val currentState = _state.value
        if (currentState.status != GameStatus.PLAYING) return
        timerJob?.cancel()
        _state.value = currentState.copy(status = GameStatus.PAUSED)
    }

    fun resumeGame() {
        val currentState = _state.value
        if (currentState.status != GameStatus.PAUSED) return
        _state.value = currentState.copy(status = GameStatus.PLAYING)
        startTimer()
    }

    /**
     * Triggers haptic feedback if haptics are enabled in settings.
     */
    private inline fun haptic(action: HapticEngine.() -> Unit) {
        if (appSettings.hapticsEnabled) {
            hapticEngine.action()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        audioEngine.stopAmbient()
        audioEngine.release()
    }
}
