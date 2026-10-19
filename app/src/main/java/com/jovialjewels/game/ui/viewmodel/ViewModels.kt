package com.jovialjewels.game.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jovialjewels.game.data.GameData
import com.jovialjewels.game.data.GameRepository
import com.jovialjewels.game.logic.GameLogic
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Minimal factory: no DI framework, the Application passes the dependencies in. */
class SimpleFactory<T : ViewModel>(private val creator: () -> T) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = creator() as VM
}

/** Immutable snapshot of saved progress used by menus, levels, results summary and settings. */
data class ProgressUiState(
    val loaded: Boolean = false,
    val quizBest: Map<String, Int> = emptyMap(),
    val pairsBest: Map<String, Int> = emptyMap(),
    val soundEnabled: Boolean = true,
) {
    val pairsCleared: Int get() = GameLogic.completedLevelCount(pairsBest)
    fun quizBestFor(categoryId: String): Int? = quizBest[categoryId]
    fun bestPairs(level: Int): Int = GameLogic.bestPairs(level, pairsBest)
    fun isUnlocked(level: Int): Boolean = GameLogic.isLevelUnlocked(level, pairsBest)
    fun isCleared(level: Int): Boolean = GameLogic.isLevelCleared(level, pairsBest)

    companion object {
        fun from(data: GameData) = ProgressUiState(
            loaded = true,
            quizBest = data.quizBest,
            pairsBest = data.pairsBest,
            soundEnabled = data.settings.soundEnabled,
        )
    }
}

class ProgressViewModel(private val repository: GameRepository) : ViewModel() {

    val state: StateFlow<ProgressUiState> = repository.data
        .map { ProgressUiState.from(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgressUiState())

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setSoundEnabled(enabled) }
    }

    fun resetAll() {
        viewModelScope.launch { repository.resetAll() }
    }

    companion object {
        fun factory(repository: GameRepository) = SimpleFactory { ProgressViewModel(repository) }
    }
}
