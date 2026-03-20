package com.laguipemo.nefroped.core.domain.model.course

data class ClinicalCase(
    val id: String,
    val topicId: String,
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val quizId: String? = null // El ID del Quiz que contiene la interactividad del caso
)

data class ComplementaryResource(
    val id: String,
    val topicId: String,
    val title: String,
    val url: String
)
