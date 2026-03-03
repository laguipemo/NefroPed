package com.laguipemo.nefroped.core.domain.model.course

data class Topic(
    val id: String,
    val title: String,
    val imageUrl: String,
    val description: String,
    val lessons: List<LessonId>,
    val conversationId: String,  // ← Para chat específico del tema
    val order: Int
)

data class LessonId(
    val id: String
)
