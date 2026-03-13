package com.laguipemo.nefroped.core.domain.usecase.course

import com.laguipemo.nefroped.core.domain.model.course.Lesson
import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository

class GetLessonsUseCase(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(topicId: String): List<Lesson> = repository.getLessons(topicId)
}
