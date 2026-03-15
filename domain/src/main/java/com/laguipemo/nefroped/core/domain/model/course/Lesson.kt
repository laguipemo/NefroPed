package com.laguipemo.nefroped.core.domain.model.course

data class Lesson(
    val id: String,
    val topicId: String,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val imagePlaceholder: String?,
    val contentUrl: String, // La URL al .md
    val videoUrl: String? = null,
    val audioUrl: String? = null,
    val pdfUrl: String? = null,
    val order: Int,
    val isCompleted: Boolean = false
)
