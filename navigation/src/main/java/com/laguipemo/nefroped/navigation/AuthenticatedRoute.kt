package com.laguipemo.nefroped.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AuthenticatedRoute {

    @Serializable
    data object Course : AuthenticatedRoute

    @Serializable
    data class Lessons(val topicId: String) : AuthenticatedRoute

    @Serializable
    data class LessonDetail(val lessonId: String) : AuthenticatedRoute

    @Serializable
    data class Quiz(
        val id: String, 
        val isTopicId: Boolean = true,
        val title: String? = null // Nuevo campo para el título inmediato
    ) : AuthenticatedRoute

    @Serializable
    data class ClinicalCaseList(val topicId: String) : AuthenticatedRoute

    @Serializable
    data object Profile : AuthenticatedRoute

    @Serializable
    data class Chat(val conversationId: String) : AuthenticatedRoute

    @Serializable
    data object ResetPassword : AuthenticatedRoute
}
