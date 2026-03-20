package com.laguipemo.nefroped.core.domain.model.course

data class Topic(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val imagePlaceholder: String? = null,
    val contentUrl: String?,
    val indexContent: String?,
    val order: Int,
    val type: TopicType = TopicType.LESSONS, // Para distinguir el Tema 4
    val conversationId: String? = null,
    val lessonsCount: Int = 0,
    val completedLessonsCount: Int = 0
) {
    val progress: Float
        get() = if (lessonsCount > 0) completedLessonsCount.toFloat() / lessonsCount else 0f
}
