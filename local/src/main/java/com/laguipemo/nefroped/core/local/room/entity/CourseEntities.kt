package com.laguipemo.nefroped.core.local.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val imagePlaceholder: String?,
    val contentUrl: String?,
    val indexContent: String?,
    val order: Int,
    val type: String, // "lessons" o "clinical_cases"
    val conversationId: String?,
    val lessonsCount: Int,
    val completedLessonsCount: Int
)

@Entity(
    tableName = "lessons",
    foreignKeys = [
        ForeignKey(
            entity = TopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["topicId"])]
)
data class LessonEntity(
    @PrimaryKey val id: String,
    val topicId: String,
    val title: String,
    val imageUrl: String?,
    val imagePlaceholder: String?,
    val description: String?,
    val content: String,
    val videoUrl: String?,
    val audioUrl: String?,
    val pdfUrl: String?,
    val order: Int,
    val isCompleted: Boolean = false
)
