package com.jovialjewels.game.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jovialjewels.game.audio.Sfx
import com.jovialjewels.game.audio.SoundManager
import com.jovialjewels.game.data.GameRepository
import com.jovialjewels.game.logic.GameLogic
import com.jovialjewels.game.logic.LevelConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PairCard(
    val id: Int,
    val item: Int,
    val faceUp: Boolean = false,
    val matched: Boolean = false,
)

enum class PairsOutcome { WIN, LOSE }

/** Immutable Matching Pairs screen state. */
data class PairsUiState(
    val level: Int = 1,
    val columns: Int = 2,
    val totalPairs: Int = 0,
    val cards: List<PairCard> = emptyList(),
    val matchedPairs: Int = 0,
    val millisLeft: Long = 0L,
    val totalMillis: Long = 1L,
    val paused: Boolean = false,
    val inputLocked: Boolean = false,
    val outcome: PairsOutcome? = null,
) {
    val secondsLeft: Int get() = ((millisLeft + 999) / 1000).toInt()
    val timeFraction: Float get() = (millisLeft.toFloat() / totalMillis).coerceIn(0f, 1f)
    val lowTime: Boolean get() = millisLeft <= 10_000L
}

class PairsViewModel(
    private val config: LevelConfig,
    private val repository: GameRepository,
    private val sound: SoundManager,
) : ViewModel() {

    private val _state = MutableStateFlow(PairsUiState())
    val state: StateFlow<PairsUiState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var flipBackJob: Job? = null
    private var finishJob: Job? = null

    init {
        restart()
    }

    fun restart() {
        timerJob?.cancel()
        flipBackJob?.cancel()
        finishJob?.cancel()
        val deck = GameLogic.buildDeck(config.pairs)
        val total = config.seconds * 1000L
        _state.value = PairsUiState(
            level = config.level,
            columns = config.columns,
            totalPairs = config.pairs,
            cards = deck.mapIndexed { index, item -> PairCard(id = index, item = item) },
            millisLeft = total,
            totalMillis = total,
        )
        startTimer()
    }

    fun pause() = _state.update { if (it.outcome == null) it.copy(paused = true) else it }

    fun resume() = _state.update { it.copy(paused = false) }

    fun flip(cardId: Int) {
        val s = _state.value
        if (s.paused || s.inputLocked || s.outcome != null) return
        val card = s.cards.firstOrNull { it.id == cardId } ?: return
        if (card.faceUp || card.matched) return
        val open = s.cards.filter { it.faceUp && !it.matched }
        if (open.size >= 2) return

        sound.play(Sfx.CLICK)
        val flipped = s.cards.map { if (it.id == cardId) it.copy(faceUp = true) else it }
        if (open.isEmpty()) {
            _state.value = s.copy(cards = flipped)
            return
        }

        val first = open.first()
        if (GameLogic.isMatch(first.item, card.item)) {
            val matchedCards = flipped.map {
                if (it.id == first.id || it.id == cardId) it.copy(faceUp = true, matched = true) else it
            }
            val matched = s.matchedPairs + 1
            _state.value = s.copy(cards = matchedCards, matchedPairs = matched)
            if (matched >= s.totalPairs) finish(PairsOutcome.WIN) else sound.play(Sfx.MATCH)
        } else {
            _state.value = s.copy(cards = flipped, inputLocked = true)
            flipBackJob = viewModelScope.launch {
                delay(MISMATCH_DELAY_MS)
                _state.update { st ->
                    st.copy(
                        cards = st.cards.map { if (!it.matched) it.copy(faceUp = false) else it },
                        inputLocked = false,
                    )
                }
            }
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(TICK_MS)
                val s = _state.value
                if (s.outcome != null) break
                if (s.paused) {
                    _state.first { !it.paused || it.outcome != null }
                    continue
                }
                val left = (s.millisLeft - TICK_MS).coerceAtLeast(0L)
                _state.update { it.copy(millisLeft = left) }
                if (left == 0L) {
                    finish(PairsOutcome.LOSE)
                    break
                }
            }
        }
    }

    private fun finish(outcome: PairsOutcome) {
        if (_state.value.outcome != null) return
        timerJob?.cancel()
        flipBackJob?.cancel()
        sound.play(if (outcome == PairsOutcome.WIN) Sfx.WIN else Sfx.LOSE)
        val matched = _state.value.matchedPairs
        _state.update { it.copy(inputLocked = true) }
        finishJob = viewModelScope.launch {
            repository.savePairsBest(config.level, matched)
            _state.update { it.copy(outcome = outcome) }
        }
    }

    companion object {
        const val TICK_MS = 100L
        const val MISMATCH_DELAY_MS = 700L

        fun factory(config: LevelConfig, repository: GameRepository, sound: SoundManager) =
            SimpleFactory { PairsViewModel(config, repository, sound) }
    }
}
