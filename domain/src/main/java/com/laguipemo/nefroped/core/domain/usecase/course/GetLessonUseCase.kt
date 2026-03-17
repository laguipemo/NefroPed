package com.laguipemo.nefroped.core.domain.usecase.course

import com.laguipemo.nefroped.core.domain.model.course.Lesson
import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository
import kotlinx.coroutines.flow.Flow

class GetLessonUseCase(
    private val repository: CourseRepository
) {
    operator fun invoke(lessonId: String): Flow<Lesson?> = repository.observeLesson(lessonId)
}
