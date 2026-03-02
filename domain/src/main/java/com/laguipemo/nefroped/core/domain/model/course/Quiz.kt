package com.laguipemo.nefroped.core.domain.model.course

data class Quiz(
    val id: String,
    val title: String,
    val questions: List<Question>
)

data class Question(
    val id: String,
    val text: String,
    val options: List<QuestionOption>,
    val correctAnswerIndex: Int
)

data class QuestionOption(
    val id: String,
    val text: String
)
