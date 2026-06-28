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
    val streak: Int = 0,
    val lastPointsGained: Int = 0,
    val questionNumber: Int = 24,
    val totalQuestions: Int = 50
)

/**
 * ViewModel for the Quiz feature.
 */
class QuizViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    fun selectOption(index: Int) {
        if (!_uiState.value.isAnswerChecked) {
            _uiState.update { it.copy(selectedOptionIndex = index) }
        }
    }

    fun checkAnswer() {
        val state = _uiState.value
        if (state.selectedOptionIndex == null || state.isAnswerChecked) return

        val isCorrect = state.selectedOptionIndex == state.currentQuestion.correctOptionIndex
        val points = if (isCorrect) 150 else 0 // Match screenshot pts
        
        _uiState.update { currentState ->
            currentState.copy(
                isAnswerChecked = true,
                score = currentState.score + points,
                streak = if (isCorrect) currentState.streak + 1 else 0,
                lastPointsGained = points
            )
        }
    }

    fun nextQuestion() {
        _uiState.update { currentState ->
            currentState.copy(
                currentQuestion = QuizEngine.generateQuestion(),
                selectedOptionIndex = null,
                isAnswerChecked = false,
                questionNumber = (currentState.questionNumber % currentState.totalQuestions) + 1,
                lastPointsGained = 0
            )
        }
    }
}
