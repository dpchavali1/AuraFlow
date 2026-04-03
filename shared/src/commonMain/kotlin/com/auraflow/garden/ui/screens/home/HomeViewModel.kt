package com.auraflow.garden.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auraflow.garden.data.model.WorldType
import com.auraflow.garden.ui.theme.WorldTheme
import com.auraflow.garden.ui.theme.worldThemeFor
import com.auraflow.garden.data.repository.PlayerRepository
import com.auraflow.garden.game.StageManager
import com.auraflow.garden.game.StageProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val stageProgressList: List<StageProgress> = emptyList(),
    val worldName: String = WorldType.WHISPERING_MEADOW.displayName,
    val currentWorld: WorldType = WorldType.WHISPERING_MEADOW,
    val clearedCount: Int = 0,
    val totalStages: Int = 5,
    val isLoading: Boolean = true,
    val nextIncompleteStageId: Int? = null,
)

class HomeViewModel(
    private val stageManager: StageManager,
    private val playerRepository: PlayerRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadProgress()
    }

    fun loadProgress() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val stageList = stageManager.getStageProgressList()
            val clearedCount = stageList.count { it.bestStars >= 1 }
            val nextIncomplete = stageList.firstOrNull { it.isUnlocked && it.bestStars == 0 }?.stageId
            val currentWorld = WorldType.entries.firstOrNull { nextIncomplete in it.stageRange }
                ?: WorldType.entries.firstOrNull { stageList.lastOrNull()?.stageId in it.stageRange }
                ?: WorldType.WHISPERING_MEADOW
            _uiState.value = HomeUiState(
                stageProgressList = stageList,
                worldName = currentWorld.displayName,
                currentWorld = currentWorld,
                clearedCount = clearedCount,
                totalStages = stageList.size,
                isLoading = false,
                nextIncompleteStageId = nextIncomplete,
            )
        }
    }

    fun onStageClear(stageId: Int) {
        loadProgress()
    }
}
