package com.laguipemo.nefroped.core.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class TopicDto(
    val id: String, 
    val title: String, 
    val description: String, 
    @SerialName("image_url") val imageUrl: String? = null, 
    @SerialName("image_placeholder") val imagePlaceholder: String? = null, 
    @SerialName("content_url") val contentUrl: String? = null, 
    val order: Int, 
    val type: String? = "lessons", 
    @SerialName("conversation_id") val conversationId: String? = null
)

@Serializable
internal data class LessonDto(
    val id: String, 
    @SerialName("topic_id") val topicId: String, 
    val title: String, 
    @SerialName("image_url") val imageUrl: String? = null, 
    @SerialName("image_placeholder") val imagePlaceholder: String? = null, 
    val description: String? = null, 
    @SerialName("content_url") val contentUrl: String, 
    @SerialName("video_url") val videoUrl: String? = null, 
    @SerialName("audio_url") val audioUrl: String? = null, 
    @SerialName("pdf_url") val pdfUrl: String? = null, 
    val order: Int
)

@Serializable
internal data class QuizDto(
    val id: String, 
    @SerialName("topic_id") val topicId: String, 
    val title: String, 
    val description: String? = null
)

@Serializable
internal data class QuestionDto(
    val id: String, 
    @SerialName("quiz_id") val quizId: String, 
    val text: String, 
    val type: String, 
    val options: JsonElement, 
    @SerialName("correct_answer") val correctAnswer: JsonElement, 
    val explanation: String? = null, 
    val intro: String? = null
)

@Serializable
internal data class QuizResultDto(
    @SerialName("user_id") val userId: String, 
    @SerialName("quiz_id") val quizId: String, 
    val score: Float, 
    @SerialName("correct_answers") val correctAnswers: Int, 
    @SerialName("total_questions") val totalQuestions: Int, 
    @SerialName("completed_at") val completedAt: String
)

@Serializable
internal data class ClinicalCaseDto(
    val id: String, 
    @SerialName("topic_id") val topicId: String, 
    val title: String, 
    val description: String, 
    @SerialName("image_url") val imageUrl: String? = null, 
    @SerialName("quiz_id") val quizId: String? = null
)

@Serializable
internal data class ComplementaryResourceDto(
    val id: String, 
    @SerialName("topic_id") val topicId: String, 
    val title: String, 
    val url: String
)

@Serializable
internal data class UserProgressDto(
    @SerialName("user_id") val userId: String, 
    @SerialName("lesson_id") val lessonId: String
)
