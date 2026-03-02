package com.laguipemo.nefroped.core.domain.model.course

data class Lesson(
    val id: String,
    val title: String,
    val imageUrl: String,
    val topicsPreview: List<String>,  // Índice de tópicos
    val markdownContentUrl: String?,  // Local/Supabase/GitHub
    val order: Int
)
