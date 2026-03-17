package com.laguipemo.nefroped.core.domain.repository.course

import com.laguipemo.nefroped.core.domain.model.course.Lesson
import com.laguipemo.nefroped.core.domain.model.course.Topic
import kotlinx.coroutines.flow.Flow

interface CourseRepository {
    fun observeTopics(): Flow<List<Topic>>
    fun observeLessons(topicId: String): Flow<List<Lesson>>
    fun observeLesson(lessonId: String): Flow<Lesson?>
    
    suspend fun syncTopics(): Result<Unit>
    suspend fun syncLessons(topicId: String): Result<Unit>
    
    suspend fun markLessonAsCompleted(lessonId: String): Boolean
}