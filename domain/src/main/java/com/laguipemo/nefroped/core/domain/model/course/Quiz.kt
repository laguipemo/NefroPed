package com.laguipemo.nefroped.core.domain.model.course

enum class QuestionType {
    TRUE_FALSE, ONE_CHOICE, MULTIPLE_CHOICE, MATCH_DEFINITION
}

data class Quiz(
    val id: String,
    val topicId: String,
    val title: String,
    val description: String? = null, // Para la historia clínica en Casos Clínicos
    val questions: List<Question> = emptyList()
)

data class Question(
    val id: String,
    val quizId: String,
    val text: String,
    val intro: String? = null,
    val type: QuestionType,
    val options: QuestionOptions,
    val correctAnswer: QuestionAnswer,
    val explanation: String? = null
)

sealed interface QuestionOptions {
    data class Simple(val list: List<String>) : QuestionOptions
    data class Match(val terms: List<String>, val definitions: List<String>) : QuestionOptions
}

sealed interface QuestionAnswer {
    data class Single(val index: Int) : QuestionAnswer
    data class Multiple(val indices: List<Int>) : QuestionAnswer
    data class Match(val mapping: Map<Int, Int>) : QuestionAnswer
}

data class QuizResult(
    val quizId: String,
    val score: Float,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val completedAt: Long
)
