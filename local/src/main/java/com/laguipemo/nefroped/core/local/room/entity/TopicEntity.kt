package com.laguipemo.nefroped.core.local.room.entity

import androidx.room.Entity
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
    val conversationId: String?,
    val lessonsCount: Int,
    val completedLessonsCount: Int
)
