package com.laguipemo.nefroped.core.domain.repository.course

import com.laguipemo.nefroped.core.domain.model.course.Lesson
import com.laguipemo.nefroped.core.domain.model.course.Quiz
import com.laguipemo.nefroped.core.domain.model.course.Topic

interface CourseRepository {
    interface CourseRepository {
        suspend fun getTopics(): List<Topic>
        suspend fun getLessons(topicId: String): List<Lesson>
        suspend fun getLessonContent(lessonId: String): String  // Markdown
        suspend fun getQuiz(topicId: String): Quiz
    }
}