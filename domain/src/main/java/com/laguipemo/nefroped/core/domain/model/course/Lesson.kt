package com.laguipemo.nefroped.core.domain.model.course

data class Lesson(
    val id: String,
    val topicId: String,
    val title: String,
    val imageUrl: String?,
    val imagePlaceholder: String? = null,
    val description: String? = null,
    val content: String, // Cambiado de contentUrl a content
    val videoUrl: String? = null,
    val audioUrl: String? = null,
    val pdfUrl: String? = null,
    val order: Int,
    val isCompleted: Boolean = false
)
