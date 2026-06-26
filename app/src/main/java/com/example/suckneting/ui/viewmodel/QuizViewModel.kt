package com.example.suckneting.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.suckneting.domain.quiz.QuizEngine
import com.example.suckneting.domain.quiz.SubnetQuestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * UI State for the Quiz/Practice Module.
 */
data class QuizUiState(
    val currentQuestion: SubnetQuestion = QuizEngine.generateQuestion(),
    val selectedOptionIndex: Int? = null,
    val isAnswerChecked: Boolean = false,
    val score: Int = 0,
    val streak: Int = 0
)

/**
 * ViewModel for the Quiz feature.
 * Manages game logic, scoring, and question transitions.
 */
class QuizViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    /**
     * Updates the currently selected option.
     */
    fun selectOption(index: Int) {
        if (!_uiState.value.isAnswerChecked) {
            _uiState.update { it.copy(selectedOptionIndex = index) }
        }
    }

    /**
     * Validates the selected answer and updates score/streak.
     */
    fun checkAnswer() {
        val state = _uiState.value
        if (state.selectedOptionIndex == null || state.isAnswerChecked) return

        val isCorrect = state.selectedOptionIndex == state.currentQuestion.correctOptionIndex
        
        _uiState.update { currentState ->
            currentState.copy(
                isAnswerChecked = true,
                score = if (isCorrect) currentState.score + 10 else currentState.score,
                streak = if (isCorrect) currentState.streak + 1 else 0
            )
        }
    }

    /**
     * Generates a new question and resets selection state.
     */
    fun nextQuestion() {
        _uiState.update { currentState ->
            currentState.copy(
                currentQuestion = QuizEngine.generateQuestion(),
                selectedOptionIndex = null,
                isAnswerChecked = false
            )
        }
    }
}
