package com.laguipemo.nefroped.core.domain.model.course

data class Lesson(
    val id: String,
    val topicId: String,
    val title: String,
    val imageUrl: String?,
    val description: String?,
    val content: String, // Markdown o URL
    val order: Int,
    val isCompleted: Boolean = false
)
