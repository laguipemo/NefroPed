package com.laguipemo.nefroped.core.domain.usecase.course

import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetQuizAverageUseCase(
    private val repository: CourseRepository
) {
    operator fun invoke(): Flow<Float?> = repository.observeAllQuizResults().map { results ->
        if (results.isEmpty()) null
        else results.map { it.score }.average().toFloat()
    }
}
