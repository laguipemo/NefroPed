package com.laguipemo.nefroped.features.course.quiz

import com.laguipemo.nefroped.core.domain.model.course.Quiz
import com.laguipemo.nefroped.core.domain.model.course.QuizResult

sealed interface QuizUiState {
    data object Loading : QuizUiState
    data class Error(val message: String) : QuizUiState
    
    data class Content(
        val quiz: Quiz,
        val currentQuestionIndex: Int = 0,
        val currentSelection: UserSelection = UserSelection.None,
        val answers: Map<Int, UserSelection> = emptyMap(),
        val isFinished: Boolean = false,
        val quizResult: QuizResult? = null,
        val isSubmitting: Boolean = false
    ) : QuizUiState {
        val currentQuestion = quiz.questions.getOrNull(currentQuestionIndex)
        val progress = if (quiz.questions.isNotEmpty()) (currentQuestionIndex + 1).toFloat() / quiz.questions.size else 0f
        val isLastQuestion = currentQuestionIndex == quiz.questions.size - 1
        val canContinue = currentSelection !is UserSelection.None
    }
}

sealed interface UserSelection {
    data object None : UserSelection
    data class Single(val index: Int) : UserSelection
    data class Multiple(val indices: Set<Int>) : UserSelection
    data class Match(val mapping: Map<Int, Int>) : UserSelection
}
