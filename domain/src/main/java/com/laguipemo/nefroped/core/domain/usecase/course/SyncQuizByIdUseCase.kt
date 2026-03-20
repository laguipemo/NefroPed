package com.laguipemo.nefroped.core.domain.usecase.course

import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository

class SyncQuizByIdUseCase(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(quizId: String): Result<Unit> = repository.syncQuizById(quizId)
}
