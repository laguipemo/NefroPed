package com.laguipemo.nefroped.core.domain.usecase.course

import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository

class MarkLessonAsCompletedUseCase(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(lessonId: String): Boolean = repository.markLessonAsCompleted(lessonId)
}
