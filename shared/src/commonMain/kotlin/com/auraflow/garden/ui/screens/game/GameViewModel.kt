package com.auraflow.garden.ui.screens.game

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auraflow.garden.data.local.StageResult
import com.auraflow.garden.data.model.GameState
import com.auraflow.garden.data.model.GameStatus
import com.auraflow.garden.data.model.Link
import com.auraflow.garden.data.model.Node
import com.auraflow.garden.data.repository.LevelRepository
import com.auraflow.garden.data.repository.PlayerRepository
import com.auraflow.garden.data.settings.AppSettings
import com.auraflow.garden.game.DifficultyAnalyzer
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
    private val difficultyAnalyzer: DifficultyAnalyzer,
) : ViewModel() {

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<GameEvent>()
    val events: SharedFlow<GameEvent> = _events.asSharedFlow()

    private var timerJob: Job? = null
    private var mechanicsJob: Job? = null
    private var startTimeMs: Long = 0L
    private var energyWarningSent = false

    // Pressure node tracking: nodeId -> epoch ms when current countdown started
    private val pressureStartMs = mutableMapOf<String, Long>()

    // Moving node oscillation: nodeId -> 0..2 (0->1 forward, 1->2 backward)
    private val movingNodeOscillation = mutableMapOf<String, Float>()

    // ── Stage loading with adaptive difficulty ────────────────────────────────

    fun loadStage(stageId: Int) {
        timerJob?.cancel()
        mechanicsJob?.cancel()
        _state.value = GameState(status = GameStatus.LOADING)

        viewModelScope.launch {
            // Fetch recent results for this stage band and compute difficulty profile
            val recentResults = playerRepository.getAllStageResults()
                .filter { it.stageId >= maxOf(1, stageId - 4) && it.stageId <= stageId }
            val profile = difficultyAnalyzer.analyze(recentResults)

            val baseLevel = levelRepository.loadLevel(stageId)

            // Apply adaptive adjustments only when player has a history
            val level = if (recentResults.isNotEmpty()) {
                val adjustedMultiplier = difficultyAnalyzer.adjustedEnergyMultiplier(
                    baseLevel.energyCostMultiplier, profile
                )
                val energyScale = when {
                    difficultyAnalyzer.shouldEaseNextStage(profile) -> 1.10f   // +10% energy when struggling
                    difficultyAnalyzer.shouldHardenNextStage(profile) -> 0.92f // -8% when dominating
                    else -> 1f
                }
                baseLevel.copy(
                    energyCostMultiplier = adjustedMultiplier,
                    maxEnergy = baseLevel.maxEnergy * energyScale,
                )
            } else {
                baseLevel
            }

            val nodes = levelRepository.levelToNodes(level)

            pressureStartMs.clear()
            movingNodeOscillation.clear()
            val loadTimeMs = Clock.System.now().toEpochMilliseconds()
            for (node in nodes) {
                if (node.isPressureNode && node.pressureDurationMs > 0L) {
                    pressureStartMs[node.id] = loadTimeMs
                }
                if (!node.movementPath.isNullOrEmpty()) {
                    movingNodeOscillation[node.id] = 0f
                }
            }

            _state.value = GameState(
                level = level,
                nodes = nodes,
                links = emptyList(),
                energy = level.maxEnergy,
                maxEnergy = level.maxEnergy,
                score = 0,
                elapsedMs = 0L,
                status = GameStatus.PLAYING,
            )
            energyWarningSent = false
            audioEngine.setVolume(appSettings.sfxVolume, appSettings.musicVolume)
            audioEngine.playAmbient(level.world)
            startTimer()
            startMechanicsLoop()
        }
    }

    // ── Timer — anchored so pause/resume loses zero elapsed time ─────────────

    private fun startTimer() {
        timerJob?.cancel()
        // Anchor: startTimeMs = now - elapsedMs, so (now - startTimeMs) == preserved elapsed immediately
        startTimeMs = Clock.System.now().toEpochMilliseconds() - _state.value.elapsedMs
        timerJob = viewModelScope.launch {
            while (_state.value.status == GameStatus.PLAYING) {
                delay(100L)
                val elapsed = Clock.System.now().toEpochMilliseconds() - startTimeMs
                _state.value = _state.value.copy(elapsedMs = elapsed)
            }
        }
    }

    // ── Mechanics loop — pressure expiry + moving nodes ──────────────────────

    private fun startMechanicsLoop() {
        mechanicsJob?.cancel()
        mechanicsJob = viewModelScope.launch {
            while (true) {
                delay(80L)
                val state = _state.value
                when (state.status) {
                    GameStatus.WON, GameStatus.FAILED -> break
                    GameStatus.PLAYING -> Unit
                    else -> continue
                }

                val nowMs = Clock.System.now().toEpochMilliseconds()
                var updatedState = state

                // Pressure node countdown
                val progressMap = mutableMapOf<String, Float>()
                for (node in state.nodes) {
                    if (!node.isPressureNode || node.isLinked || node.pressureDurationMs <= 0L) continue

                    val startMs = pressureStartMs.getOrPut(node.id) { nowMs }
                    val progress = ((nowMs - startMs).toFloat() / node.pressureDurationMs.toFloat())
                        .coerceIn(0f, 1f)
                    progressMap[node.id] = progress

                    if (progress >= 1f) {
                        val penalty = updatedState.maxEnergy * 0.06f
                        val newEnergy = (updatedState.energy - penalty).coerceAtLeast(0f)
                        updatedState = updatedState.copy(energy = newEnergy)
                        pressureStartMs[node.id] = nowMs
                        haptic { warningBuzz() }
                        _events.emit(GameEvent.PressureExpired(node.id))

                        if (newEnergy <= 0f && !GameEngine.isSolved(updatedState.nodes)) {
                            _state.value = updatedState.copy(
                                status = GameStatus.FAILED,
                                pressureProgress = progressMap,
                            )
                            _events.emit(GameEvent.LevelFailed)
                            return@launch
                        }
                    }
                }

                // Moving node positions (pendulum along movementPath)
                val deltaFrac = 80f / 16_000f
                var updatedNodes = updatedState.nodes
                var nodesChanged = false

                for ((index, node) in updatedState.nodes.withIndex()) {
                    val path = node.movementPath
                    if (path.isNullOrEmpty() || path.size < 2 || node.isLinked) continue

                    val speed = node.movementSpeedDps.coerceAtLeast(0.02f)
                    val osc = ((movingNodeOscillation[node.id] ?: 0f) + speed * deltaFrac) % 2f
                    movingNodeOscillation[node.id] = osc

                    val pathT = if (osc <= 1f) osc else 2f - osc
                    val segCount = path.size - 1
                    val rawSeg = pathT * segCount
                    val segIdx = rawSeg.toInt().coerceIn(0, segCount - 1)
                    val segFrac = rawSeg - segIdx
                    val from = path[segIdx]
                    val to = path[(segIdx + 1).coerceAtMost(path.size - 1)]
                    val newPos = Offset(from.x + (to.x - from.x) * segFrac, from.y + (to.y - from.y) * segFrac)

                    if (newPos != node.position) {
                        val m = updatedNodes.toMutableList()
                        m[index] = node.copy(position = newPos)
                        updatedNodes = m
                        nodesChanged = true
                    }
                }

                if (nodesChanged) updatedState = updatedState.copy(nodes = updatedNodes)
                _state.value = updatedState.copy(pressureProgress = progressMap)
            }
        }
    }

    // ── Input handlers ────────────────────────────────────────────────────────

    fun onNodeTapped(nodeId: String) {
        val s = _state.value
        if (s.status != GameStatus.PLAYING) return
        haptic { lightTap() }

        if (!s.connectMode) {
            _state.value = s.copy(connectMode = true, selectedNodeId = nodeId)
            return
        }
        val selectedId = s.selectedNodeId
        if (selectedId == null) { _state.value = s.copy(selectedNodeId = nodeId); return }
        if (selectedId == nodeId) { _state.value = s.copy(selectedNodeId = null); return }
        attemptLink(selectedId, nodeId)
        _state.value = _state.value.copy(selectedNodeId = null)
    }

    fun onDragStart(nodeId: String) {
        val s = _state.value
        if (s.status != GameStatus.PLAYING) return
        _state.value = s.copy(selectedNodeId = nodeId)
    }

    fun onDragEnd(targetNodeId: String?) {
        val s = _state.value
        if (s.status != GameStatus.PLAYING) return
        val sourceId = s.selectedNodeId ?: return
        if (targetNodeId != null && targetNodeId != sourceId) attemptLink(sourceId, targetNodeId)
        _state.value = _state.value.copy(selectedNodeId = null)
    }

    // ── Core link logic ───────────────────────────────────────────────────────

    private fun attemptLink(sourceId: String, targetId: String) {
        val s = _state.value
        val nodes = s.nodes
        val src = nodes.find { it.id == sourceId } ?: return
        val tgt = nodes.find { it.id == targetId } ?: return

        if (src.colorType != tgt.colorType || src.pairedNodeId != targetId) {
            emitLinkFailed(src.position); return
        }
        if (src.isLinked || tgt.isLinked) { emitLinkFailed(src.position); return }

        val energyCost = GameEngine.calculateEnergyCost(
            src.position, tgt.position, s.level?.energyCostMultiplier ?: 1f,
        )
        if (energyCost > s.energy) { emitLinkFailed(src.position); return }

        // Count new intersections (always tracked; hard-blocked only when required)
        val newIntersections = s.links.count { link ->
            val ls = nodes.find { it.id == link.sourceNodeId }
            val lt = nodes.find { it.id == link.targetNodeId }
            if (ls == null || lt == null) return@count false
            if (link.sourceNodeId == sourceId || link.targetNodeId == sourceId ||
                link.sourceNodeId == targetId || link.targetNodeId == targetId
            ) return@count false
            GameEngine.checkIntersection(src.position, tgt.position, ls.position, lt.position)
        }
        if (s.level?.noIntersectionsRequired == true && newIntersections > 0) {
            emitLinkFailed(src.position); return
        }

        val newLink = Link(
            id = "${sourceId}_${targetId}",
            sourceNodeId = sourceId,
            targetNodeId = targetId,
            colorType = src.colorType,
            pathPoints = listOf(src.position, tgt.position),
            energyCost = energyCost,
        )
        val newEnergy = (s.energy - energyCost).coerceAtLeast(0f)
        val updatedNodes = nodes.map { n ->
            when (n.id) { sourceId, targetId -> n.copy(isLinked = true); else -> n }
        }
        val updatedIntersections = s.intersectionCount + newIntersections
        val level = s.level
        val newScore = if (level != null && GameEngine.isSolved(updatedNodes)) {
            GameEngine.calculateScore(newEnergy, s.maxEnergy, s.elapsedMs, level.parTime * 1000L)
        } else s.score

        _state.value = s.copy(
            nodes = updatedNodes,
            links = s.links + newLink,
            energy = newEnergy,
            score = newScore,
            intersectionCount = updatedIntersections,
        )

        if (src.isPressureNode) pressureStartMs.remove(sourceId)
        if (tgt.isPressureNode) pressureStartMs.remove(targetId)

        viewModelScope.launch {
            haptic { mediumTap() }
            audioEngine.playLinkDrawn(src.colorType)
            _events.emit(GameEvent.LinkDrawn)

            if (GameEngine.isSolved(updatedNodes)) {
                val isCrescendo = GameEngine.isCrescendo(
                    updatedNodes, newEnergy, s.maxEnergy, updatedIntersections
                )
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
                mechanicsJob?.cancel()

                if (level != null) {
                    val stars = GameEngine.calculateStars(newEnergy, s.maxEnergy, level.starThresholds)
                    saveStageResult(
                        stageId = level.stageId, stars = stars, score = newScore,
                        energyRemaining = newEnergy, elapsedMs = _state.value.elapsedMs,
                        isPerfectClear = stars == 3, isCrescendo = isCrescendo,
                    )
                }
                return@launch
            }

            val energyFraction = newEnergy / s.maxEnergy.coerceAtLeast(1f)
            if (energyFraction <= 0.1f && !energyWarningSent) {
                energyWarningSent = true
                haptic { warningBuzz() }
                _events.emit(GameEvent.EnergyLow(energyFraction))
            }

            if (newEnergy <= 0f && !GameEngine.isSolved(updatedNodes)) {
                haptic { heavyThud() }
                audioEngine.playLevelFailed()
                _state.value = _state.value.copy(status = GameStatus.FAILED)
                _events.emit(GameEvent.LevelFailed)
                timerJob?.cancel()
                mechanicsJob?.cancel()
                if (level != null) {
                    saveStageResult(
                        stageId = level.stageId, stars = 0, score = s.score,
                        energyRemaining = newEnergy, elapsedMs = _state.value.elapsedMs,
                        isPerfectClear = false, isCrescendo = false,
                    )
                }
            }
        }
    }

    private fun emitLinkFailed(nodePosition: Offset) {
        viewModelScope.launch {
            haptic { heavyThud() }
            audioEngine.playLinkFailed()
            _events.emit(GameEvent.LinkFailed)
            _state.value = _state.value.copy(fizzleAt = nodePosition)
            delay(80L)
            if (_state.value.fizzleAt == nodePosition) _state.value = _state.value.copy(fizzleAt = null)
        }
    }

    // ── Undo ──────────────────────────────────────────────────────────────────

    fun undoLastLink() {
        val s = _state.value
        if (s.status != GameStatus.PLAYING || s.links.isEmpty()) return
        val last = s.links.last()
        val restoredEnergy = (s.energy + last.energyCost).coerceAtMost(s.maxEnergy)
        val newLinks = s.links.dropLast(1)
        val updatedNodes = s.nodes.map { n ->
            if (n.id == last.sourceNodeId || n.id == last.targetNodeId) {
                if (n.isPressureNode && n.pressureDurationMs > 0L) {
                    pressureStartMs[n.id] = Clock.System.now().toEpochMilliseconds()
                }
                n.copy(isLinked = false)
            } else n
        }
        _state.value = s.copy(
            links = newLinks,
            nodes = updatedNodes,
            energy = restoredEnergy,
            intersectionCount = recalculateIntersections(newLinks, updatedNodes),
        )
        energyWarningSent = restoredEnergy <= s.maxEnergy * 0.1f
    }

    fun undoAll() {
        val s = _state.value
        if (s.status != GameStatus.PLAYING) return
        val nowMs = Clock.System.now().toEpochMilliseconds()
        val resetNodes = s.nodes.map { n ->
            if (n.isPressureNode && n.pressureDurationMs > 0L) pressureStartMs[n.id] = nowMs
            n.copy(isLinked = false)
        }
        _state.value = s.copy(links = emptyList(), nodes = resetNodes, energy = s.maxEnergy, intersectionCount = 0)
        energyWarningSent = false
    }

    private fun recalculateIntersections(links: List<Link>, nodes: List<Node>): Int {
        var count = 0
        for (i in links.indices) {
            val a = links[i]
            val aFrom = nodes.find { it.id == a.sourceNodeId }?.position ?: continue
            val aTo = nodes.find { it.id == a.targetNodeId }?.position ?: continue
            for (j in i + 1 until links.size) {
                val b = links[j]
                val bFrom = nodes.find { it.id == b.sourceNodeId }?.position ?: continue
                val bTo = nodes.find { it.id == b.targetNodeId }?.position ?: continue
                if (a.sourceNodeId == b.sourceNodeId || a.sourceNodeId == b.targetNodeId ||
                    a.targetNodeId == b.sourceNodeId || a.targetNodeId == b.targetNodeId
                ) continue
                if (GameEngine.checkIntersection(aFrom, aTo, bFrom, bTo)) count++
            }
        }
        return count
    }

    // ── Pause / resume ────────────────────────────────────────────────────────

    fun toggleConnectMode() {
        val s = _state.value
        _state.value = s.copy(connectMode = !s.connectMode, selectedNodeId = null)
    }

    fun pauseGame() {
        val s = _state.value
        if (s.status != GameStatus.PLAYING) return
        timerJob?.cancel()
        mechanicsJob?.cancel()
        _state.value = s.copy(status = GameStatus.PAUSED)
    }

    fun resumeGame() {
        val s = _state.value
        if (s.status != GameStatus.PAUSED) return
        _state.value = s.copy(status = GameStatus.PLAYING)
        startTimer()         // anchors startTimeMs = now - elapsedMs → no time lost
        startMechanicsLoop()
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    private fun saveStageResult(
        stageId: Int, stars: Int, score: Int, energyRemaining: Float,
        elapsedMs: Long, isPerfectClear: Boolean, isCrescendo: Boolean,
    ) {
        viewModelScope.launch {
            val result = StageResult(
                stageId = stageId, stars = stars, score = score,
                energyRemaining = energyRemaining, elapsedMs = elapsedMs,
                isPerfectClear = isPerfectClear, isCrescendo = isCrescendo,
                completedAtMs = Clock.System.now().toEpochMilliseconds(),
            )
            playerRepository.saveStageResult(result)

            if (stars >= 1) {
                val progress = playerRepository.getProgress()
                playerRepository.updateProgress(
                    progress.copy(
                        highestStageCleared = maxOf(progress.highestStageCleared, stageId),
                        totalCrescendos = if (isCrescendo) progress.totalCrescendos + 1 else progress.totalCrescendos,
                        totalPerfectClears = if (isPerfectClear) progress.totalPerfectClears + 1 else progress.totalPerfectClears,
                    )
                )
                val streakEvent = streakManager.onStageClear()
                when (streakEvent) {
                    is StreakEvent.StreakUpdated -> _events.emit(GameEvent.StreakUpdated(streakEvent.streakDays))
                    is StreakEvent.StreakMilestone -> _events.emit(GameEvent.StreakMilestone(streakEvent.days))
                    is StreakEvent.StreakReset -> _events.emit(GameEvent.StreakReset)
                }
                val allResults = playerRepository.getAllStageResults()
                val newAchievements = achievementSystem.checkAfterStageClear(
                    result, playerRepository.getProgress(), allResults
                )
                if (newAchievements.isNotEmpty()) _events.emit(GameEvent.AchievementsUnlocked(newAchievements))
            }
        }
    }

    private inline fun haptic(action: HapticEngine.() -> Unit) {
        if (appSettings.hapticsEnabled) hapticEngine.action()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        mechanicsJob?.cancel()
        audioEngine.stopAmbient()
        audioEngine.release()
    }
}
