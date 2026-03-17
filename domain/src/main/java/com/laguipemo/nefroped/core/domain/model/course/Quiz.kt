package com.laguipemo.nefroped.core.domain.model.course

data class Quiz(
    val id: String,
    val topicId: String,
    val title: String,
    val questions: List<Question> = emptyList()
)

data class Question(
    val id: String,
    val quizId: String,
    val text: String,
    val options: List<QuestionOption>,
    val correctAnswerIndex: Int,
    val explanation: String? = null
)

data class QuestionOption(
    val id: String,
    val text: String
)

data class QuizResult(
    val quizId: String,
    val score: Float,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val completedAt: Long
)
