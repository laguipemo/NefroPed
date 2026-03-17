package com.laguipemo.nefroped.features.course.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.laguipemo.nefroped.core.domain.model.course.Quiz
import com.laguipemo.nefroped.core.domain.model.course.QuizResult
import com.laguipemo.nefroped.core.domain.usecase.course.GetQuizUseCase
import com.laguipemo.nefroped.core.domain.usecase.course.SubmitQuizUseCase
import com.laguipemo.nefroped.core.domain.usecase.course.SyncQuizUseCase
import com.laguipemo.nefroped.navigation.AuthenticatedRoute
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class QuizViewModel(
    savedStateHandle: SavedStateHandle,
    private val getQuizUseCase: GetQuizUseCase,
    private val syncQuizUseCase: SyncQuizUseCase,
    private val submitQuizUseCase: SubmitQuizUseCase
) : ViewModel() {

    private val topicId: String = savedStateHandle.toRoute<AuthenticatedRoute.Quiz>().topicId

    private val _currentQuestionIndex = MutableStateFlow(0)
    private val _selectedOptionIndex = MutableStateFlow<Int?>(null)
    private val _answers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    private val _isSubmitting = MutableStateFlow(false)
    private val _isFinished = MutableStateFlow(false)
    private val _quizResult = MutableStateFlow<QuizResult?>(null)

    val uiState: StateFlow<QuizUiState> = combine(
        getQuizUseCase(topicId),
        _currentQuestionIndex,
        _selectedOptionIndex,
        _answers,
        _isSubmitting,
        _isFinished,
        _quizResult
    ) { params: Array<Any?> ->
        val quiz = params[0] as Quiz?
        val index = params[1] as Int
        val selected = params[2] as Int?
        val answers = params[3] as Map<Int, Int>
        val submitting = params[4] as Boolean
        val finished = params[5] as Boolean
        val result = params[6] as QuizResult?

        if (quiz == null) {
            QuizUiState.Loading
        } else {
            QuizUiState.Content(
                quiz = quiz,
                currentQuestionIndex = index,
                selectedOptionIndex = selected,
                answers = answers,
                isSubmitting = submitting,
                isFinished = finished,
                quizResult = result
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = QuizUiState.Loading
    )

    init {
        viewModelScope.launch {
            syncQuizUseCase(topicId)
        }
    }

    fun onOptionSelected(index: Int) {
        _selectedOptionIndex.value = index
    }

    fun onNextQuestion() {
        val currentState = uiState.value as? QuizUiState.Content ?: return
        val selected = _selectedOptionIndex.value ?: return
        
        val newAnswers = currentState.answers + (currentState.currentQuestionIndex to selected)
        _answers.value = newAnswers
        
        if (currentState.isLastQuestion) {
            submitQuiz(currentState.quiz.id, newAnswers, currentState.quiz.questions.size)
        } else {
            _currentQuestionIndex.value = currentState.currentQuestionIndex + 1
            _selectedOptionIndex.value = null
        }
    }

    private fun submitQuiz(quizId: String, answers: Map<Int, Int>, totalQuestions: Int) {
        viewModelScope.launch {
            val currentState = uiState.value as? QuizUiState.Content ?: return@launch
            _isSubmitting.value = true
            
            val correctCount = currentState.quiz.questions.indices.count { i ->
                val question = currentState.quiz.questions[i]
                answers[i] == question.correctAnswerIndex
            }
            
            val result = QuizResult(
                quizId = quizId,
                score = (correctCount.toFloat() / totalQuestions) * 10,
                correctAnswers = correctCount,
                totalQuestions = totalQuestions,
                completedAt = System.currentTimeMillis()
            )
            
            val success = submitQuizUseCase(result)
            if (success) {
                _quizResult.value = result
                _isFinished.value = true
            }
            _isSubmitting.value = false
        }
    }
}
