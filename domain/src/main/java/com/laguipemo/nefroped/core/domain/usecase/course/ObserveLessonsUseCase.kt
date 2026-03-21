package com.laguipemo.nefroped.core.domain.usecase.course

import com.laguipemo.nefroped.core.domain.model.course.Lesson
import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository
import kotlinx.coroutines.flow.Flow

class ObserveLessonsUseCase(
    private val repository: CourseRepository
) {
    operator fun invoke(topicId: String): Flow<List<Lesson>> = repository.observeLessons(topicId)
}
