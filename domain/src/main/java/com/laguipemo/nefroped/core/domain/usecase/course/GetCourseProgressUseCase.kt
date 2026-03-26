package com.laguipemo.nefroped.core.domain.usecase.course

import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetCourseProgressUseCase(
    private val repository: CourseRepository
) {
    operator fun invoke(): Flow<Pair<Int, Int>> = repository.observeTopics().map { topics ->
        val total = topics.sumOf { it.lessonsCount }
        val completed = topics.sumOf { it.completedLessonsCount }
        completed to total
    }
}
