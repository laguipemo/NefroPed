package com.laguipemo.nefroped.core.domain.repository.course

import com.laguipemo.nefroped.core.domain.model.course.Lesson
import com.laguipemo.nefroped.core.domain.model.course.Topic
import kotlinx.coroutines.flow.Flow

interface CourseRepository {
    suspend fun getTopics(): List<Topic>
    suspend fun getLessons(topicId: String): List<Lesson>
    suspend fun markLessonAsCompleted(lessonId: String): Boolean
    fun observeTopicProgress(topicId: String): Flow<Float>
}