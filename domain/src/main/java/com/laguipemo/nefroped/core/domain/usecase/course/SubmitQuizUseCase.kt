package com.laguipemo.nefroped.core.domain.usecase.course

import com.laguipemo.nefroped.core.domain.model.course.QuizResult
import com.laguipemo.nefroped.core.domain.repository.course.CourseRepository

class SubmitQuizUseCase(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(result: QuizResult): Boolean = repository.saveQuizResult(result)
}
