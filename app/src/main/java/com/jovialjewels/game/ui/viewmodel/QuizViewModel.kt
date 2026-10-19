package com.jovialjewels.game.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jovialjewels.game.audio.Sfx
import com.jovialjewels.game.audio.SoundManager
import com.jovialjewels.game.data.GameRepository
import com.jovialjewels.game.logic.QuizCategory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuizOption(val text: String, val isCorrect: Boolean)

/** Immutable quiz screen state. */
data class QuizUiState(
    val categoryTitle: String = "",
    val questionIndex: Int = 0,
    val total: Int = 0,
    val question: String = "",
    val options: List<QuizOption> = emptyList(),
    val selectedIndex: Int? = null,
    val score: Int = 0,
    val paused: Boolean = false,
    val finishedScore: Int? = null,
) {
    val answered: Boolean get() = selectedIndex != null
    val progressFraction: Float get() = if (total == 0) 0f else (questionIndex + if (answered) 1 else 0).toFloat() / total
    val counterText: String get() = "${(questionIndex + 1).coerceAtMost(total)}/$total"
}

class QuizViewModel(
    private val category: QuizCategory,
    private val repository: GameRepository,
    private val sound: SoundManager,
) : ViewModel() {

    private var prepared: List<List<QuizOption>> = emptyList()
    private val _state = MutableStateFlow(QuizUiState())
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    private var advanceJob: Job? = null

    init {
        restart()
    }

    fun restart() {
        advanceJob?.cancel()
        prepared = category.questions.map { q ->
            (listOf(QuizOption(q.correct, true)) + q.wrong.map { QuizOption(it, false) }).shuffled()
        }
        _state.value = stateFor(index = 0, score = 0)
    }

    fun answer(optionIndex: Int) {
        val s = _state.value
        if (s.paused || s.answered || s.finishedScore != null) return
        val option = s.options.getOrNull(optionIndex) ?: return
        sound.play(if (option.isCorrect) Sfx.CORRECT else Sfx.WRONG)
        _state.value = s.copy(selectedIndex = optionIndex, score = s.score + if (option.isCorrect) 1 else 0)
        advanceJob = viewModelScope.launch {
            delay(ADVANCE_DELAY_MS)
            _state.first { !it.paused } // auto-advance waits while paused
            advance()
        }
    }

    fun pause() = _state.update { if (it.finishedScore == null) it.copy(paused = true) else it }

    fun resume() = _state.update { it.copy(paused = false) }

    private suspend fun advance() {
        val s = _state.value
        val next = s.questionIndex + 1
        if (next >= prepared.size) {
            repository.saveQuizBest(category.id, s.score)
            _state.update { it.copy(finishedScore = s.score) }
        } else {
            _state.value = stateFor(index = next, score = s.score)
        }
    }

    private fun stateFor(index: Int, score: Int): QuizUiState {
        val question = category.questions.getOrNull(index)
        return QuizUiState(
            categoryTitle = category.title,
            questionIndex = index,
            total = category.questions.size,
            question = question?.text.orEmpty(),
            options = prepared.getOrNull(index).orEmpty(),
            score = score,
        )
    }

    companion object {
        const val ADVANCE_DELAY_MS = 750L

        fun factory(category: QuizCategory, repository: GameRepository, sound: SoundManager) =
            SimpleFactory { QuizViewModel(category, repository, sound) }
    }
}
