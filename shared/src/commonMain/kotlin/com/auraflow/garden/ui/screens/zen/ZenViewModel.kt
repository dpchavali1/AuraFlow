package com.auraflow.garden.ui.screens.zen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auraflow.garden.data.local.AuraFlowDatabase
import com.auraflow.garden.data.local.BlueprintEntity
import com.auraflow.garden.data.model.NodeColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Clock

class ZenViewModel(private val database: AuraFlowDatabase) : ViewModel() {

    private val _uiState = MutableStateFlow(ZenUiState())
    val uiState: StateFlow<ZenUiState> = _uiState.asStateFlow()

    private var autosaveJob: kotlinx.coroutines.Job? = null

    fun selectColor(color: NodeColor) {
        _uiState.value = _uiState.value.copy(selectedColor = color)
    }

    fun setGridSize(size: Int) {
        _uiState.value = _uiState.value.copy(gridSize = size.coerceIn(3, 8))
    }

    fun placeNode(x: Float, y: Float) {
        val state = _uiState.value
        val gridStep = 1f / state.gridSize
        val snappedX = (x / gridStep).let { kotlin.math.round(it) * gridStep }.coerceIn(0f, 1f)
        val snappedY = (y / gridStep).let { kotlin.math.round(it) * gridStep }.coerceIn(0f, 1f)

        // Check for node at same position
        val existing = state.nodes.find {
            kotlin.math.abs(it.x - snappedX) < 0.01f && kotlin.math.abs(it.y - snappedY) < 0.01f
        }
        if (existing != null) {
            // Tap on existing node — remove it (and its links)
            removeNode(existing.id)
            return
        }

        val newId = "zen_${Clock.System.now().toEpochMilliseconds()}_${state.nodes.size}"
        val newNode = BlueprintNode(
            id = newId,
            color = state.selectedColor.name,
            x = snappedX,
            y = snappedY,
        )
        _uiState.value = state.copy(nodes = state.nodes + newNode, isDirty = true)
        scheduleAutosave()
    }

    fun linkNodes(sourceId: String, targetId: String) {
        val state = _uiState.value
        val source = state.nodes.find { it.id == sourceId } ?: return
        val target = state.nodes.find { it.id == targetId } ?: return
        if (source.color != target.color) return  // Only link same colors in Zen
        if (sourceId == targetId) return

        // Check if link already exists
        val alreadyLinked = state.links.any {
            (it.sourceId == sourceId && it.targetId == targetId) ||
                (it.sourceId == targetId && it.targetId == sourceId)
        }
        if (alreadyLinked) {
            // Toggle off
            val filtered = state.links.filter {
                !((it.sourceId == sourceId && it.targetId == targetId) ||
                    (it.sourceId == targetId && it.targetId == sourceId))
            }
            _uiState.value = state.copy(links = filtered, isDirty = true)
        } else {
            val newLink = BlueprintLink(sourceId = sourceId, targetId = targetId, color = source.color)
            _uiState.value = state.copy(links = state.links + newLink, isDirty = true)
        }
        scheduleAutosave()
    }

    fun removeNode(nodeId: String) {
        val state = _uiState.value
        val filteredNodes = state.nodes.filter { it.id != nodeId }
        val filteredLinks = state.links.filter { it.sourceId != nodeId && it.targetId != nodeId }
        _uiState.value = state.copy(nodes = filteredNodes, links = filteredLinks, isDirty = true)
        scheduleAutosave()
    }

    fun clearAll() {
        _uiState.value = _uiState.value.copy(nodes = emptyList(), links = emptyList(), isDirty = true)
        scheduleAutosave()
    }

    fun setBlueprintName(name: String) {
        _uiState.value = _uiState.value.copy(blueprintName = name)
    }

    fun saveBlueprint() {
        viewModelScope.launch {
            val state = _uiState.value
            val now = Clock.System.now().toEpochMilliseconds()
            val nodesJson = Json.encodeToString(state.nodes)
            val linksJson = Json.encodeToString(state.links)

            val blueprint = BlueprintEntity(
                id = state.savedBlueprintId ?: 0,
                name = state.blueprintName,
                nodesJson = nodesJson,
                linksJson = linksJson,
                gridSize = state.gridSize,
                createdAtMs = now,
                updatedAtMs = now,
            )
            val savedId = database.blueprintDao().insert(blueprint)
            _uiState.value = _uiState.value.copy(savedBlueprintId = savedId, isDirty = false)
        }
    }

    fun loadBlueprint(id: Long) {
        viewModelScope.launch {
            val entity = database.blueprintDao().getById(id) ?: return@launch
            val nodes = Json.decodeFromString<List<BlueprintNode>>(entity.nodesJson)
            val links = Json.decodeFromString<List<BlueprintLink>>(entity.linksJson)
            _uiState.value = ZenUiState(
                nodes = nodes,
                links = links,
                gridSize = entity.gridSize,
                blueprintName = entity.name,
                savedBlueprintId = entity.id,
                isDirty = false,
            )
        }
    }

    private fun scheduleAutosave() {
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            delay(3000L)  // 3 second debounce
            if (_uiState.value.isDirty) {
                saveBlueprint()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (_uiState.value.isDirty) {
            viewModelScope.launch { saveBlueprint() }
        }
    }
}
