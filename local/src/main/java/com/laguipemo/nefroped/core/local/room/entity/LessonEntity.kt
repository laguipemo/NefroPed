package com.laguipemo.nefroped.core.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: String,
    val topicId: String,
    val title: String,
    val imageUrl: String?,
    val imagePlaceholder: String?,
    val description: String?,
    val content: String, // La URL del .md
    val videoUrl: String?,
    val audioUrl: String?,
    val pdfUrl: String?,
    val order: Int,
    val isCompleted: Boolean
)
